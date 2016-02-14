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
package fr.zcraft.zbanque.structure.containers;

import fr.zcraft.zbanque.utils.LocationUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A chest or a hopper in the bank, to be understood as a Minecraft chest/hopper.
 *
 * @see Silo Containers are stored inside silos.
 */
public class Container
{
    /**
     * The chest type. Either {@link Material#CHEST}, {@link Material#TRAPPED_CHEST} or {@link Material#HOPPER}.
     */
    private Material containerType = null;

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
    private final Map<BlockType, Integer> content = new ConcurrentHashMap<>();


    /**
     * This constructor must be called from the Bukkit main thread.
     *
     * @param mainLocation      The main chest location: the only one if this chest/hopper is a single one;
     *                          the exposed one (if it makes sense) else.
     * @param secondaryLocation The secondary location. {@code null} if this chest is a single
     *                          chest or a hopper.
     * @param bypassChecks      If true, no coherence check will be performed and this constructor
     *                          will become thread safe. <strong>You must be <em>ABSOLUTELY
     *                          SURE</em> the data provided is valid if this is set to true, to
     *                          avoid data corruption!</strong>
     *
     * @throws IllegalArgumentException if the main location is null, or if some of the given
     *                                  locations don't point to a chest/hopper, or if this chest is not a
     *                                  single chest.
     */
    public Container(Location mainLocation, Location secondaryLocation, boolean bypassChecks)
    {
        if (!bypassChecks)
        {
            Validate.notNull(mainLocation, "The main location cannot be null");
            Validate.isTrue(isValidContainerLocation(mainLocation), "The main location must be a chest/hopper");
            Validate.isTrue(isValidContainerLocation(secondaryLocation), "The secondary location must be null or a chest/hopper");
            Validate.isTrue(secondaryLocation == null || mainLocation.getWorld().equals(secondaryLocation.getWorld()), "The secondary location must be in the same world as the first one");
        }

        this.mainLocation = LocationUtils.cloneLocationToBlock(mainLocation);
        this.secondaryLocation = LocationUtils.cloneLocationToBlock(secondaryLocation);

        if (!bypassChecks)
        {
            // The chest type will be updated later, it's not a problem.
            checkChest();
            Validate.notNull(containerType, "The chest is not a single chest!");
        }
    }

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
    public Container(Location mainLocation, Location secondaryLocation)
    {
        this(mainLocation, secondaryLocation, false);
    }

    public Location getMainLocation()
    {
        return mainLocation;
    }

    public Location getSecondaryLocation()
    {
        return secondaryLocation;
    }

    public Map<BlockType, Integer> getContent()
    {
        return content;
    }

    /**
     * @return This container's type. May be {@code null} if not initialized or invalid.
     */
    public Material getContainerType()
    {
        return containerType;
    }

    /**
     * @return {@code true} if this container is a chest.
     */
    public boolean isChest()
    {
        return containerType != null && (containerType == Material.CHEST || containerType == Material.TRAPPED_CHEST);
    }

    /**
     * Reads the chest content and updates the stored content.
     *
     * <p>Must be called from the Bukkit main thread.</p>
     *
     * @throws IllegalStateException if the chest is no longer a chest or hopper.
     */
    public void update()
    {
        try
        {
            Validate.isTrue(isValidContainerLocation(mainLocation), "Invalid main container location");
            Validate.isTrue(isValidContainerLocation(secondaryLocation), "Invalid secondary chest location");

            checkChest();
            Validate.notNull(containerType, "Invalid container type");
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("This chest at " + (secondaryLocation == null ? mainLocation.toString() : mainLocation + " ; " + secondaryLocation) + " is no longer a valid chest", e);
        }

        content.clear();

        final Inventory inventory = ((InventoryHolder) mainLocation.getBlock().getState()).getInventory();
        for (ItemStack stack : inventory)
        {
            if (stack != null && stack.getType() != Material.AIR)
            {
                BlockType type = new BlockType(stack);

                if (!content.containsKey(type))
                    content.put(type, stack.getAmount());
                else
                    content.put(type, content.get(type) + stack.getAmount());
            }
        }
    }

    /**
     * Updates the amount of a specific block type.
     *
     * <p>For import from file only, this method should not be used out of this specific case.</p>
     *
     * @param type The type.
     * @param amount The amount of items of this type in this container.
     */
    public void updateBlockType(BlockType type, Integer amount)
    {
        content.put(type, amount);
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
    private boolean isValidContainerLocation(Location location)
    {
        if (location == null)
            return true;

        final Material type = location.getBlock().getType();
        return type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.HOPPER;
    }

    /**
     * Updates the container type (normal, trapped or hopper). Sets it to {@code null} if the chest is no longer
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
            containerType = mainType;
        else
            containerType = null;

        // Checks if the chest is still coherent
        if (secondaryLocation != null)
        {
            if (mainLocation.distanceSquared(secondaryLocation) != 1)
                containerType = null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Container container = (Container) o;

        return mainLocation.equals(container.mainLocation) && !(secondaryLocation != null ? !secondaryLocation.equals(container.secondaryLocation) : container.secondaryLocation != null);
    }

    @Override
    public int hashCode()
    {
        return 31 * mainLocation.hashCode() + (secondaryLocation != null ? secondaryLocation.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return containerType + " @ " + LocationUtils.userFriendlyLocation(mainLocation)
                + (secondaryLocation != null ? " & " + LocationUtils.userFriendlyLocation(secondaryLocation) : "");
    }
}
