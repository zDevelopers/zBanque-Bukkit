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
package fr.zcraft.zbanque;

import fr.zcraft.zbanque.utils.BlockUtils;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.tools.PluginLogger;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;


public class Config
{
    public static final ConfigurationItem<String> BANK_WORLD_RAW    = ConfigurationItem.item("bank.world", "world");
    public static final ConfigurationItem<String> BANK_CORNER_1_RAW = ConfigurationItem.item("bank.firstCorner", "");
    public static final ConfigurationItem<String> BANK_CORNER_2_RAW = ConfigurationItem.item("bank.otherCorner", "");


    // --- Computed values

    private static Location BANK_LOWEST_CORNER = null;
    private static Location BANK_HIGHEST_CORNER = null;


    /**
     * Initializes the configuration, computing values if needed.
     */
    static void initialize()
    {
        final World world = Bukkit.getWorld(BANK_WORLD_RAW.get());
        if (world == null)
        {
            PluginLogger.error("Cannot load bank corners: the world {0} does not exists.", BANK_WORLD_RAW.get());
            ZBanque.get().abort();
            return;
        }

        try
        {
            Location corner1 = BlockUtils.string2Location(world, BANK_CORNER_1_RAW.get());
            Location corner2 = BlockUtils.string2Location(world, BANK_CORNER_2_RAW.get());

            updateBankCorners(corner1, corner2);
        }
        catch (IllegalArgumentException e)
        {
            PluginLogger.error("Cannot load bank corners: invalid corner coordinates.");
            ZBanque.get().abort();
            return;
        }
    }

    /**
     * Updates the bank corners.
     *
     * @param corner1 The first corner.
     * @param corner2 The other corner.
     *
     * @throws IllegalArgumentException if the two locations are not in the same world.
     */
    public static void updateBankCorners(Location corner1, Location corner2)
    {
        Validate.isTrue(corner1.getWorld().equals(corner2.getWorld()), "The corners must be in the same world");

        BANK_HIGHEST_CORNER = new Location(
                corner1.getWorld(),
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ())
        );

        BANK_LOWEST_CORNER = new Location(
                corner1.getWorld(),
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ())
        );

        BANK_WORLD_RAW.set(corner1.getWorld().getName());
        BANK_CORNER_1_RAW.set(BlockUtils.location2String(BANK_HIGHEST_CORNER));
        BANK_CORNER_2_RAW.set(BlockUtils.location2String(BANK_LOWEST_CORNER));
    }

    public static Location getBankLowestCorner()
    {
        return BANK_LOWEST_CORNER;
    }

    public static Location getBankHighestCorner()
    {
        return BANK_HIGHEST_CORNER;
    }
}
