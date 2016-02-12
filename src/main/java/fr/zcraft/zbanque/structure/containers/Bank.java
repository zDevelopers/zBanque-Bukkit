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

import fr.zcraft.zbanque.structure.update.BankAreaCollector;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;


public class Bank
{
    private final String codeName;
    private final String displayName;

    private final Location lowestCorner;
    private final Location highestCorner;

    private final Set<Silo> silos = new CopyOnWriteArraySet<>();


    public Bank(final String codeName, final String displayName, final Location corner1, final Location corner2)
    {
        this.codeName = codeName;
        this.displayName = displayName;

        this.highestCorner = new Location(
                corner1.getWorld(),
                Math.max(corner1.getX(), corner2.getX()),
                Math.min(Math.max(corner1.getY(), corner2.getY()), corner1.getWorld().getMaxHeight()),
                Math.max(corner1.getZ(), corner2.getZ())
        );

        this.lowestCorner = new Location(
                corner1.getWorld(),
                Math.min(corner1.getX(), corner2.getX()),
                Math.max(Math.min(corner1.getY(), corner2.getY()), 0),
                Math.min(corner1.getZ(), corner2.getZ())
        );
    }

    public String getCodeName()
    {
        return codeName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public Location getLowestCorner()
    {
        return lowestCorner;
    }

    public Location getHighestCorner()
    {
        return highestCorner;
    }

    public Set<Silo> getSilos()
    {
        return Collections.unmodifiableSet(silos);
    }

    public Map<BlockType, Long> getContent()
    {
        Map<BlockType, Long> bankContent = new HashMap<>();
        for (Silo silo : getSilos())
        {
            for (Map.Entry<BlockType, Integer> content : silo.getContent().entrySet())
            {
                Long value = bankContent.containsKey(content.getKey()) ? bankContent.get(content.getKey()) : 0l;
                value += content.getValue();

                bankContent.put(content.getKey(), value);
            }
        }

        return bankContent;
    }

    public Set<Map.Entry<BlockType, Long>> getOrderedContent(Boolean reverseOrder)
    {
        final int reverse = reverseOrder ? -1 : 1;

        Set<Map.Entry<BlockType, Long>> bankContentSorted = new TreeSet<>(new Comparator<Map.Entry<BlockType, Long>>()
        {
            @Override
            public int compare(Map.Entry<BlockType, Long> one, Map.Entry<BlockType, Long> other)
            {
                if (one.getValue() < other.getValue())
                    return -1 * reverse;
                else if (one.getValue() > other.getValue())
                    return reverse;
                else
                    return 0;
            }
        });

        bankContentSorted.addAll(getContent().entrySet());
        return bankContentSorted;
    }

    public Long getTotalItemsCount()
    {
        Long total = 0l;

        for (Long amount : getContent().values())
            total += amount;

        return total;
    }

    public void addSilo(Silo silo)
    {
        silos.add(silo);
    }

    public void removeSilo(Silo silo)
    {
        silos.remove(silo);
    }


    /**
     * Scans and updates the bank structure.
     *
     * @param requestedBy The requester of this update. Messages related to the update process will
     *                    be sent to this user.
     * @param verbose     If {@code true}, a verbose log will be sent.
     */
    public void updateStructure(CommandSender requestedBy, boolean verbose)
    {
        requestedBy.sendMessage(I.t("{cst}{bold}Starting bank structure update for {0}.", getDisplayName()));
        RunTask.timer(new BankAreaCollector(this, requestedBy, verbose), 1l, 1l);
    }
}
