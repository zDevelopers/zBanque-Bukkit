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
package fr.zcraft.zbanque.containers;

import fr.zcraft.zbanque.utils.BlockUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A chest in the bank, to be understood as a Minecraft chest.
 *
 * @see Silo Chests are stored inside silos.
 */
public class Chest
{
    /**
     * The chest type. Either {@link Material#CHEST} or {@link Material#TRAPPED_CHEST}.
     */
    private Material chestType;

    /**
     * The main chest location: the only one if this chest is a single one; the exposed one (if it
     * makes sense) else.
     */
    private final Location mainLocation;

    /**
     * The secondary location. {@code null} if this chest is a single chest.
     */
    private Location secondaryLocation = null;

    /**
     * The content of this chest.
     */
    private final Map<StackType, Integer> content = new ConcurrentHashMap<>();


    /**
     * This constructor must be called from the Bukkit main thread.
     *
     * @param mainLocation      The main chest location: the only one if this chest is a single one;
     *                          the exposed one (if it makes sense) else.
     * @param secondaryLocation The secondary location. {@code null} if this chest is a single
     *                          chest.
     *
     * @throws IllegalArgumentException if the main location is null, or if some of the given
     *                                  locations don't point to a chest, or if this chest is not a
     *                                  single chest.
     */
    public Chest(Location mainLocation, Location secondaryLocation)
    {
        Validate.notNull(mainLocation, "The main location cannot be null");
        Validate.isTrue(isValidChestLocation(mainLocation), "The main location must be a chest");
        Validate.isTrue(isValidChestLocation(secondaryLocation), "The secondary location must be null or a chest");
        Validate.isTrue(secondaryLocation == null || mainLocation.getWorld().equals(secondaryLocation.getWorld()), "The secondary location must be in the same world as the first one");

        this.mainLocation = BlockUtils.cloneLocationToBlock(mainLocation);
        this.secondaryLocation = BlockUtils.cloneLocationToBlock(secondaryLocation);

        checkChest();
        Validate.notNull(chestType, "The chest is not a single chest!");
    }

    public Location getMainLocation()
    {
        return mainLocation;
    }

    public Location getSecondaryLocation()
    {
        return secondaryLocation;
    }

    public Map<StackType, Integer> getContent()
    {
        return content;
    }

    /**
     * Reads the chest content and updates the stored content.
     *
     * <p>Must be called from the Bukkit main thread.</p>
     *
     * @throws IllegalStateException if the chest is no longer a chest.
     */
    public void update()
    {
        try
        {
            Validate.isTrue(isValidChestLocation(mainLocation));
            Validate.isTrue(isValidChestLocation(secondaryLocation));

            checkChest();
            Validate.notNull(chestType);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("This chest at " + (secondaryLocation == null ? mainLocation.toString() : mainLocation + " ; " + secondaryLocation) + " is no longer a valid chest");
        }

        content.clear();

        final Inventory inventory = ((org.bukkit.block.Chest) mainLocation.getBlock().getState()).getInventory();
        for (ItemStack stack : inventory)
        {
            StackType type = new StackType(stack);

            if (!content.containsKey(type))
                content.put(type, stack.getAmount());
            else
                content.put(type, content.get(type) + stack.getAmount());
        }
    }


    /**
     * Checks if the given location is a valid chest.
     *
     * <p>Must be called from the Bukkit main thread.</p>
     *
     * @param location The location.
     *
     * @return {@code true} if the block at this location is a chest, or if the location is {@code
     * null}.
     */
    private boolean isValidChestLocation(Location location)
    {
        if (location == null)
            return true;

        final Material type = location.getBlock().getType();
        return type == Material.CHEST || type == Material.TRAPPED_CHEST;
    }

    /**
     * Updates the chest type (normal or trapped). Sets it to {@code null} if the chest is no longer
     * an unique chest.
     *
     * <p>Must be called from the Bukkit main thread.</p>
     */
    private void checkChest()
    {
        // Check types
        Material mainType = mainLocation.getBlock().getType();
        Material secondaryType = secondaryLocation == null ? null : secondaryLocation.getBlock().getType();

        if (secondaryType == null || mainType.equals(secondaryType))
            chestType = mainType;
        else
            chestType = null;

        // Checks if the chest is still coherent
        if (secondaryLocation != null)
        {
            if (mainLocation.distanceSquared(secondaryLocation) != 1)
                chestType = null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chest chest = (Chest) o;

        return mainLocation.equals(chest.mainLocation) && !(secondaryLocation != null ? !secondaryLocation.equals(chest.secondaryLocation) : chest.secondaryLocation != null);
    }

    @Override
    public int hashCode()
    {
        return 31 * mainLocation.hashCode() + (secondaryLocation != null ? secondaryLocation.hashCode() : 0);
    }
}
