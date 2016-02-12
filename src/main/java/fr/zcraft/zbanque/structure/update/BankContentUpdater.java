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

import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.Container;
import fr.zcraft.zbanque.structure.containers.Silo;
import fr.zcraft.zlib.tools.Callback;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;


public class BankContentUpdater extends BukkitRunnable
{
    private final Deque<Silo> silosQueue = new ArrayDeque<>();
    private final Set<Container> invalidContainers = new HashSet<>();

    private final Callback<Set<Container>> callback;

    /**
     * Must be called from a timer task every tick from the main thread. Auto-cancels.
     *
     * @param bank The bank to update.
     */
    public BankContentUpdater(Bank bank)
    {
        this(bank, null);
    }

    /**
     * Must be called from a timer task every tick from the main thread. Auto-cancels.
     *
     * @param bank The bank to update.
     * @param callback A callback, called with the invalid containers found while updating the
     */
    public BankContentUpdater(Bank bank, Callback<Set<Container>> callback)
    {
        this.silosQueue.addAll(bank.getSilos());
        this.callback = callback;
    }


    @Override
    public void run()
    {
        Silo silo = silosQueue.poll();

        if (silo != null)
        {
            for (Container container : silo.getContainers())
                try
                {
                    container.update();
                }
                catch (IllegalStateException e)
                {
                    invalidContainers.add(container);
                }
        }
        else
        {
            cancel();

            if (callback != null)
                callback.call(invalidContainers);
        }
    }
}
