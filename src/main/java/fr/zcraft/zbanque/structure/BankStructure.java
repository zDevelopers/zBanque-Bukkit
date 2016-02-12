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

import fr.zcraft.zbanque.containers.BlockType;
import fr.zcraft.zbanque.containers.Silo;
import fr.zcraft.zbanque.structure.update.BankAreaCollector;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;


public class BankStructure
{
    private static BankStructure instance = new BankStructure();

    private Set<Silo> silos = new CopyOnWriteArraySet<>();


    public static BankStructure get()
    {
        return instance;
    }

    private BankStructure() {}


    public Set<Silo> getSilos()
    {
        return Collections.unmodifiableSet(silos);
    }

    public Map<BlockType, Integer> getContent()
    {
        Map<BlockType, Integer> bankContent = new HashMap<>();
        for (Silo silo : BankStructure.get().getSilos())
        {
            for (Map.Entry<BlockType, Integer> content : silo.getContent().entrySet())
            {
                Integer value = bankContent.containsKey(content.getKey()) ? bankContent.get(content.getKey()) : 0;
                value += content.getValue();

                bankContent.put(content.getKey(), value);
            }
        }

        return bankContent;
    }

    public Set<Map.Entry<BlockType, Integer>> getOrderedContent(Boolean reverseOrder)
    {
        final int reverse = reverseOrder ? -1 : 1;

        Set<Map.Entry<BlockType, Integer>> bankContentSorted = new TreeSet<>(new Comparator<Map.Entry<BlockType, Integer>>()
        {
            @Override
            public int compare(Map.Entry<BlockType, Integer> one, Map.Entry<BlockType, Integer> other)
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
        requestedBy.sendMessage(I.t("{cst}{bold}Starting bank structure update."));
        RunTask.timer(new BankAreaCollector(requestedBy, verbose), 1l, 1l);
    }
}
