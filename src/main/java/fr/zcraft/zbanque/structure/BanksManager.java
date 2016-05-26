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

import fr.zcraft.zbanque.Config;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunAsyncTask;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public List<Bank> getBanks()
    {
        return Collections.unmodifiableList(new ArrayList<>(banks.values()));
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
     */
    public void registerBanksFromConfig()
    {
        for (Map.Entry<String, Config.BankSection> entry : Config.BANKS.entrySet())
        {
            final String code = entry.getKey();
            final Config.BankSection section = entry.getValue();

            final String displayName = section.NAME.get();

            final World world = section.WORLD.get();
            final Vector firstCorner = section.FIRST_CORNER.get();
            final Vector otherCorner = section.OTHER_CORNER.get();
            final Vector center = section.CENTER.get();


            if (!(displayName != null && world != null && firstCorner != null && otherCorner != null))
            {
                PluginLogger.error(I.t("Cannot register the {0} bank: you must provide a name and the two corners.", code));
                continue;
            }

            registerBank(new Bank(
                    code, displayName,
                    firstCorner.toLocation(world), otherCorner.toLocation(world),
                    center != null ? center.toLocation(world) : null
            ));
        }

        RunAsyncTask.nextTick(new Runnable() {
            @Override
            public void run()
            {
                loadAll();
            }
        });
    }

    /**
     * Loads all the registered banks content. This should be called asynchronously.
     */
    public void loadAll()
    {
        for (Bank bank : banks.values())
            bank.loadFromFile();
    }

    /**
     * Saves all the registered banks content. This should be called asynchronously.
     */
    public void saveAll()
    {
        for (Bank bank : banks.values())
            bank.saveToFile();
    }
}
