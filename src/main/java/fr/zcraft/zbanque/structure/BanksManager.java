/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.zcraft.zbanque.structure;

import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.utils.LocationUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class BanksManager
{
    private static BanksManager INSTANCE = new BanksManager();

    private Map<String, Bank> banks = new ConcurrentHashMap<>();


    public static BanksManager get()
    {
        return INSTANCE;
    }

    private BanksManager() {}


    /**
     * Returns a {@link Bank} from its code name.
     *
     * @param codeName The bank code name.
     *
     * @return the {@link Bank}; {@code null} if not found.
     */
    public Bank getBank(String codeName)
    {
        return banks.get(codeName);
    }

    /**
     * Retrieves and returns the registered banks.
     *
     * @return an unmodifiable set containing the registered banks.
     */
    public Set<Bank> getBanks()
    {
        return Collections.unmodifiableSet(new HashSet<>(banks.values()));
    }

    /**
     * Registers a new bank.
     *
     * @param bank The bank to register.
     *
     * @return {@code true} if a bank wasn't previously registered with this
     * name.
     */
    public boolean registerBank(Bank bank)
    {
        return banks.put(bank.getCodeName(), bank) == null;
    }

    /**
     * Registers all the banks in the given configuration section.
     *
     * <p>
     *     This method is looking for a section formatted as below:
     *
     *     <pre>
     *     bank-code-name:
     *         name: "Bank's display name"
     *         world: "Bank's world name"
     *         firstCorner: "Bank's first coordinate formatted as x;y;z"
     *         otherCorner: "Bank's other coordinate formatted as x;y;z"
     *     bank-other-code-name:
     *         # ... the same
     *     </pre>
     * </p>
     *
     * @param section The configuration section.
     */
    public void registerBanksInConfig(ConfigurationSection section)
    {
        if (section == null) return;

        for (String bankCodeName : section.getKeys(false))
        {
            if (section.isConfigurationSection(bankCodeName))
            {
                ConfigurationSection bankConfig = section.getConfigurationSection(bankCodeName);

                String name = bankConfig.getString("name");

                String rawWorld = bankConfig.getString("world");
                String rawCorner1 = bankConfig.getString("firstCorner");
                String rawCorner2 = bankConfig.getString("otherCorner");

                Location corner1;
                Location corner2;

                if (name == null || rawWorld == null || rawCorner1 == null || rawCorner2 == null)
                {
                    PluginLogger.warning(I.t("Invalid bank in config (code name: {0}), skipping.", bankCodeName));
                    continue;
                }

                final World world = Bukkit.getWorld(rawWorld);
                if (world == null)
                {
                    PluginLogger.error(I.t("Cannot load the {0} bank: the world {0} does not exists.", bankCodeName, rawWorld));
                    continue;
                }

                try
                {
                    corner1 = LocationUtils.string2Location(world, rawCorner1);
                    corner2 = LocationUtils.string2Location(world, rawCorner2);
                }
                catch (IllegalArgumentException e)
                {
                    PluginLogger.error(I.t("Cannot load the {0} bank corners: invalid corner coordinates. Error: {1}.", bankCodeName, e.getMessage()));
                    continue;
                }

                registerBank(new Bank(bankCodeName, name, corner1, corner2));
            }
        }
    }
}
