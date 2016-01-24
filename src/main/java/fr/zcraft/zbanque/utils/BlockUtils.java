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
package fr.zcraft.zbanque.utils;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;


public final class BlockUtils
{
    private BlockUtils() {}

    /**
     * Clones a location to a new one with block coordinates.
     *
     * @param location The cloned location.
     * @return The location with block coordinates only.
     */
    public static Location cloneLocationToBlock(Location location)
    {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Converts a string location (format "x;y;z" or "x;y;z;pitch" or "x;y;z;pitch;yaw") to a location object.
     *
     * @param world The world.
     * @param raw The raw string.
     * @return The location.
     * @throws IllegalArgumentException if the format is not valid.
     */
    public static Location string2Location(World world, String raw)
    {
        String[] parts = raw.split(";");
        Validate.isTrue(parts.length >= 3, "The location must contains at least three coordinates.");

        Location location = new Location(
                world,
                Double.valueOf(parts[0]),
                Double.valueOf(parts[1]),
                Double.valueOf(parts[2])
        );

        if (parts.length >= 4)
            location.setPitch(Float.valueOf(parts[3]));

        if (parts.length >= 5)
            location.setYaw(Float.valueOf(parts[4]));

        return location;
    }

    /**
     * Converts a location to a string compatible with {@link #string2Location(World, String)}.
     *
     * @param location The location.
     * @return The string version.
     * @see #string2Location(World, String) for the string format.
     */
    public static String location2String(Location location)
    {
        return location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getPitch() + ";" + location.getYaw();
    }
}
