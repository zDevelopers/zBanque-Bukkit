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
package fr.zcraft.zbanque.structure.update;

import fr.zcraft.zbanque.structure.containers.Area;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.containers.Container;
import fr.zcraft.zbanque.structure.containers.Silo;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.utils.LocationUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunAsyncTask;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;


/**
 * Analyses the bank to extract silos and chests.
 */
public class BankAnalyzer implements Runnable
{
    private final byte HOPPER_DATA_DOWN = 0;
    private final byte HOPPER_DATA_NORTH = 2;
    private final byte HOPPER_DATA_SOUTH = 3;
    private final byte HOPPER_DATA_WEST = 4;
    private final byte HOPPER_DATA_EAST = 5;

    private final CommandSender requestedBy;
    private final boolean verbose;

    private final Bank bank;
    private final Area area;
    private final World world;

    private final Set<Container> currentSilo = new HashSet<>();
    private final Set<Vector> analyzedHoppers = new HashSet<>();


    public BankAnalyzer(Bank bank, Area area, CommandSender requestedBy, boolean verbose)
    {
        this.requestedBy = requestedBy;
        this.verbose = verbose;

        this.bank = bank;
        this.area = area;
        this.world = area.getLowestCorner().getWorld();

        requestedBy.sendMessage(I.t("{cst}Analyzing the bank area..."));
    }

    @Override
    public void run()
    {
        final int xMin = area.getLowestCorner().getBlockX();
        final int yMin = area.getLowestCorner().getBlockY();
        final int zMin = area.getLowestCorner().getBlockZ();
        final int xMax = area.getHighestCorner().getBlockX();
        final int yMax = area.getHighestCorner().getBlockY();
        final int zMax = area.getHighestCorner().getBlockZ();

        for (int x = xMin; x <= xMax; x++)
        {
            if (verbose)
                requestedBy.sendMessage(I.t("{cst}{italic}Analyzing layer at x = {0} ", x));

            for (int y = yMin; y <= yMax; y++)
            {
                for (int z = zMin; z <= zMax; z++)
                {
                    final BlockType block = area.get(x, y, z);
                    if (block == null)
                    {
                        PluginLogger.warning("Block at {0};{1};{2} is null! This should not happens.", x, y, z);
                        continue;
                    }

                    final Material blockType = block.getType();
                    if (!isChest(blockType))
                        continue;

                    // Already analyzed and put into a silo
                    if (getSilo(x, y, z) != null)
                        continue;

                    final Location mainLocation = new Location(world, x, y, z);
                    final Location secondaryLocation = getOtherChestPart(mainLocation);

                    final Silo silo = new Silo();
                    final Container container = new Container(mainLocation, secondaryLocation, true);

                    currentSilo.clear();
                    analyzedHoppers.clear();

                    verb(I.t("{darkgray}Found a container from a new silo at {0}", LocationUtils.userFriendlyLocation(mainLocation)));

                    currentSilo.add(container);

                    lookupForChestsInSilo(container);

                    for (Container retrievedContainer : currentSilo)
                    {
                        verb(I.t("{darkgray}-> Adding container {0} to the silo {1}", retrievedContainer, silo));
                        silo.addContainer(retrievedContainer);
                    }

                    bank.addSilo(silo);
                }
            }
        }

        requestedBy.sendMessage(I.t("{cst}Updating containers content..."));
        RunTask.timer(new BankContentUpdater(bank, new Callback<Set<Container>>()
        {
            @Override
            public void call(final Set<Container> invalidContainers)
            {
                RunAsyncTask.nextTick(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!invalidContainers.isEmpty())
                        {
                            requestedBy.sendMessage(I.t("{ce}Some containers were invalid, see console for details"));

                            PluginLogger.warning("Invalid containers found while updating:");
                            for (Container container : invalidContainers)
                                PluginLogger.warning("- {0}", container);
                        }

                        extractMainChests();
                    }
                });
            }
        }), 1l, 1l);
    }

    private void extractMainChests()
    {
        requestedBy.sendMessage(I.t("{cst}Extracting silos main chests..."));
        for (Silo silo : bank.getSilos())
        {
            for (Container container : silo.getContainers())
            {
                Set<Vector> inputs = new HashSet<>();
                Set<Vector> outputs = new HashSet<>();

                lookupForInputsOrOutputs(inputs, outputs, container.getMainLocation().toVector());
                if (container.getSecondaryLocation() != null)
                    lookupForInputsOrOutputs(inputs, outputs, container.getSecondaryLocation().toVector());

                if (outputs.isEmpty())
                {
                    // This is a main container (no output)
                    silo.setMainOutput(container);
                    break;
                }
            }
        }

        requestedBy.sendMessage(I.t("{cst}Done. Bank structure updated successfully."));
    }


    /**
     * Lookup for all connected chests (connected through a hopper).
     *
     * @param start The chest to analyse.
     */
    private void lookupForChestsInSilo(Container start)
    {
        // First, we lookup for inputs and outputs
        Set<Vector> hoppers = new HashSet<>();

        Vector mainChest = start.getMainLocation().toVector();
        Vector secondaryChest = start.getSecondaryLocation() != null ? start.getSecondaryLocation().toVector() : null;

        lookupForInputsOrOutputs(hoppers, hoppers, mainChest);
        if (secondaryChest != null) lookupForInputsOrOutputs(hoppers, hoppers, secondaryChest);

        // Then, we check for chests connected to these connectors
        for (Vector hopper : hoppers)
        {
            lookupForConnectedChests(hopper);
        }
    }

    /**
     * Retrieves all hoppers going from or to the base location given.
     *
     * @param inputs    The input hoppers locations will be added to this list.
     * @param outputs   The output hoppers locations will be added to this list.
     * @param baseChest The base location.
     */
    private void lookupForInputsOrOutputs(Set<Vector> inputs, Set<Vector> outputs, Vector baseChest)
    {
        // Only one output possible.
        addIfInsideAndValid(outputs, area.getLocationNextTo(baseChest, BlockFace.DOWN), new BlockType(Material.HOPPER));

        // Many inputs. (Wow.)
        addIfInsideAndValid(inputs, area.getLocationNextTo(baseChest, BlockFace.UP), new BlockType(Material.HOPPER, HOPPER_DATA_DOWN));
        addIfInsideAndValid(inputs, area.getLocationNextTo(baseChest, BlockFace.NORTH), new BlockType(Material.HOPPER, HOPPER_DATA_SOUTH));
        addIfInsideAndValid(inputs, area.getLocationNextTo(baseChest, BlockFace.SOUTH), new BlockType(Material.HOPPER, HOPPER_DATA_NORTH));
        addIfInsideAndValid(inputs, area.getLocationNextTo(baseChest, BlockFace.EAST), new BlockType(Material.HOPPER, HOPPER_DATA_WEST));
        addIfInsideAndValid(inputs, area.getLocationNextTo(baseChest, BlockFace.WEST), new BlockType(Material.HOPPER, HOPPER_DATA_EAST));
    }

    /**
     * Follows hoppers paths to find connected containers.
     *
     * @param baseHopper The search starting point.
     */
    private void lookupForConnectedChests(Vector baseHopper)
    {
        final BlockType hopper;
        try
        {
            if ((hopper = area.get(baseHopper)).getType() != Material.HOPPER)
                return;
        }
        catch (IllegalArgumentException e)
        {
            return; // Out of the area
        }


        if (analyzedHoppers.contains(baseHopper))
            return;

        analyzedHoppers.add(baseHopper);


        // We look for hoppers around this one
        lookupForConnectedChests(area.getLocationNextTo(baseHopper, BlockFace.UP));

        Set<Vector> validHoppers = new HashSet<>();
        addIfInsideAndValid(validHoppers, area.getLocationNextTo(baseHopper, BlockFace.NORTH), new BlockType(Material.HOPPER, HOPPER_DATA_NORTH), new BlockType(Material.HOPPER, HOPPER_DATA_SOUTH));
        addIfInsideAndValid(validHoppers, area.getLocationNextTo(baseHopper, BlockFace.SOUTH), new BlockType(Material.HOPPER, HOPPER_DATA_NORTH), new BlockType(Material.HOPPER, HOPPER_DATA_SOUTH));
        addIfInsideAndValid(validHoppers, area.getLocationNextTo(baseHopper, BlockFace.EAST), new BlockType(Material.HOPPER, HOPPER_DATA_EAST), new BlockType(Material.HOPPER, HOPPER_DATA_WEST));
        addIfInsideAndValid(validHoppers, area.getLocationNextTo(baseHopper, BlockFace.WEST), new BlockType(Material.HOPPER, HOPPER_DATA_EAST), new BlockType(Material.HOPPER, HOPPER_DATA_WEST));

        for (Vector validHopper : validHoppers)
        {
            lookupForConnectedChests(validHopper);
        }


        // We add the containers receiving this hopper's content (if any)
        switch (hopper.getData())
        {
            case HOPPER_DATA_DOWN:
                addChestIfValid(area.getLocationNextTo(baseHopper, BlockFace.DOWN));
                break;

            case HOPPER_DATA_NORTH:
                addChestIfValid(area.getLocationNextTo(baseHopper, BlockFace.NORTH));
                break;

            case HOPPER_DATA_SOUTH:
                addChestIfValid(area.getLocationNextTo(baseHopper, BlockFace.SOUTH));
                break;

            case HOPPER_DATA_EAST:
                addChestIfValid(area.getLocationNextTo(baseHopper, BlockFace.EAST));
                break;

            case HOPPER_DATA_WEST:
                addChestIfValid(area.getLocationNextTo(baseHopper, BlockFace.WEST));
                break;
        }

        // We add the containers outputting to this hopper (if any)
        addChestIfValid(area.getLocationNextTo(baseHopper, BlockFace.UP));
    }

    /**
     * Adds the vector to the list if it is inside the area and valid regarding to the given
     * reference types.
     *
     * @param list  The list.
     * @param toAdd The vector to add.
     * @param types The types needed.
     */
    private void addIfInsideAndValid(Set<Vector> list, Vector toAdd, BlockType... types)
    {
        try
        {
            BlockType addedType = area.get(toAdd);

            if (addedType == null)
                throw new IllegalArgumentException();

            else if (types == null || types.length == 0)
            {
                list.add(toAdd);
            }

            else
                for (BlockType type : types)
                    if (type != null && type.equals(addedType))
                    {
                        list.add(toAdd);
                        break;
                    }
        }
        catch (IllegalArgumentException e)
        {
            // Out, not added
        }
    }

    /**
     * Adds the chest to the list only if valid and not in the excluded list.
     *
     * @param chestLocation The chest to add to the list.
     */
    private void addChestIfValid(Vector chestLocation)
    {
        try
        {
            BlockType candidate = area.get(chestLocation);

            if (isChest(candidate.getType()))
            {
                final Location chestMainLocation = chestLocation.toLocation(world);
                final Container newContainer = new Container(chestMainLocation, getOtherChestPart(chestMainLocation));

                if (addIfNotExcluded(currentSilo, currentSilo, newContainer))
                {
                    lookupForChestsInSilo(newContainer);
                }
            }
        }
        catch (IllegalArgumentException ignored) {}
    }

    /**
     * Adds the object to the list only if not in the excluded list.
     *
     * @param list     The main list.
     * @param excluded The list of items to be excluded.
     * @param added    The object to add to the list.
     * @param <T>      The object type.
     *
     * @return {@code true} if the object was added (non excluded and not already in the set).
     */
    private <T> boolean addIfNotExcluded(Set<T> list, Set<T> excluded, T added)
    {
        return !excluded.contains(added) && list.add(added);
    }

    private boolean isChest(Material type)
    {
        return type == Material.CHEST || type == Material.TRAPPED_CHEST;
    }

    private Location getOtherChestPart(Location mainChestLocation)
    {
        final int x = mainChestLocation.getBlockX();
        final int y = mainChestLocation.getBlockY();
        final int z = mainChestLocation.getBlockZ();

        final Material north = area.getNextTo(x, y, z, BlockFace.NORTH).getType();
        final Material south = area.getNextTo(x, y, z, BlockFace.SOUTH).getType();
        final Material east = area.getNextTo(x, y, z, BlockFace.EAST).getType();
        final Material west = area.getNextTo(x, y, z, BlockFace.WEST).getType();

        if (isChest(west) && getSilo(x - 1, y, z) == null)
            return mainChestLocation.clone().add(-1, 0, 0);
        else if (isChest(east) && getSilo(x + 1, y, z) == null)
            return mainChestLocation.clone().add(1, 0, 0);
        else if (isChest(north) && getSilo(x, y, z - 1) == null)
            return mainChestLocation.clone().add(0, 0, -1);
        else if (isChest(south) && getSilo(x, y, z + 1) == null)
            return mainChestLocation.clone().add(0, 0, 1);
        else
            return null;
    }

    private Silo getSilo(int x, int y, int z)
    {
        for (Silo silo : bank.getSilos())
        {
            for (Container container : silo.getContainers())
            {
                Location mainLoc = container.getMainLocation();
                Location secLoc = container.getSecondaryLocation();

                if (mainLoc.getBlockX() == x && mainLoc.getBlockY() == y && mainLoc.getBlockZ() == z)
                    return silo;
                else if (secLoc != null && secLoc.getBlockX() == x && secLoc.getBlockY() == y && secLoc.getBlockZ() == z)
                    return silo;
            }
        }

        return null;
    }


    /**
     * Sends a message to the requester if the verbose mode is enabled.
     *
     * @param message The message to be sent.
     */
    private void verb(String message)
    {
        if (verbose)
            requestedBy.sendMessage(message);
    }
}
