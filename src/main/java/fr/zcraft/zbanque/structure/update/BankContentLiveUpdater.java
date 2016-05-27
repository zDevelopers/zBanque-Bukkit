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

import fr.zcraft.zbanque.network.packets.PacketPlayOutSilos;
import fr.zcraft.zbanque.structure.BanksManager;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.Silo;
import fr.zcraft.zbanque.utils.LocationUtils;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;


public class BankContentLiveUpdater extends ZLibComponent implements Listener
{
    private Set<Location> updatedContainers = new HashSet<>();


    /**
     * Checks if the inventory is from a container in a bank, or is a chest
     * outside.
     *
     * @param inventory The inventory.
     *
     * @return {@code true} if in a bank.
     */
    private boolean inBank(final Inventory inventory)
    {
        if (inventory.getLocation() == null || inventory instanceof PlayerInventory) return false;

        for (Bank bank : BanksManager.get().getBanks())
            if (bank.isInside(inventory.getLocation()))
                return true;

        return false;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent ev)
    {
        if (inBank(ev.getInventory()))
            updatedContainers.add(ev.getInventory().getLocation());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent ev)
    {
        if (inBank(ev.getInventory()))
            updatedContainers.add(ev.getInventory().getLocation());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(final InventoryCloseEvent ev)
    {
        final Location location = ev.getInventory().getLocation();

        if (inBank(ev.getInventory()) && updatedContainers.contains(location))
        {
            updateLocation(location);
            updatedContainers.remove(location);
        }
    }

    @Override
    protected void onDisable()
    {
        if (!updatedContainers.isEmpty())
        {
            PluginLogger.info("Saving last containers...");

            for (Location location : updatedContainers)
            {
                updateLocation(location);
            }

            updatedContainers.clear();
        }
    }

    /**
     * Updates a container at a location: updates the internal content and sends
     * it to the webservice (if enabled).
     *
     * <p>Displays a warning if the location is not in a bank.</p>
     *
     * @param location The location to update.
     */
    private void updateLocation(final Location location)
    {
        Bank bank = null;
        Silo silo = null;

        for (Bank bankSearch : BanksManager.get().getBanks())
        {
            if ((silo = bankSearch.getSilo(location)) != null)
            {
                bank = bankSearch;
                break;
            }
        }

        if (silo == null)
        {
            PluginLogger.warning("Cannot find a silo for a container inside a bank area at {0}, is the bank structure updated?", LocationUtils.userFriendlyLocation(location));
            return;
        }


        final Bank finalBank = bank;
        final Silo finalSilo = silo;

        RunTask.nextTick(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    finalSilo.update();

                    new PacketPlayOutSilos(finalBank, finalSilo).send();
                }
                catch (IllegalStateException e)
                {
                    PluginLogger.error("A container in the silo at {0} is no longer valid, is the bank structure updated?", LocationUtils.userFriendlyLocation(location));
                }
            }
        });
    }
}
