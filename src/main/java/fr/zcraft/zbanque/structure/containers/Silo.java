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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * A set of containers linked together.
 *
 * <p>This is the main storage used here. A single chest is represented by a silo with a single
 * chest inside.</p>
 *
 * @see Container The storage units.
 */
public class Silo
{
    /**
     * The main output of the silo, where users take the resources.
     */
    private Container mainOutput;

    /**
     * The containers contained in this silo.
     */
    private final Set<Container> containers = new CopyOnWriteArraySet<>();


    public Container getMainOutput()
    {
        return mainOutput;
    }

    public void setMainOutput(Container mainOutput)
    {
        this.mainOutput = mainOutput;
    }

    public Set<Container> getContainers()
    {
        return Collections.unmodifiableSet(containers);
    }

    public void addContainer(Container container)
    {
        containers.add(container);
    }

    public void removeContainer(Container container)
    {
        containers.remove(container);
    }


    /**
     * Aggregates the silo's content from all the containers.
     *
     * <p>The content is re-aggregated on each call, so avoid many calls at once.</p>
     *
     * @return A map (stack type → amount in the whole silot) representing the content of this silo.
     */
    public Map<BlockType, Integer> getContent()
    {
        Map<BlockType, Integer> siloContent = new HashMap<>();

        for (Container container : containers)
        {
            for (Map.Entry<BlockType, Integer> chestContent : container.getContent().entrySet())
            {
                if (siloContent.containsKey(chestContent.getKey()))
                    siloContent.put(chestContent.getKey(), siloContent.get(chestContent.getKey()) + chestContent.getValue());
                else
                    siloContent.put(chestContent.getKey(), chestContent.getValue());
            }
        }

        return siloContent;
    }

    /**
     * Retrieves the block type mainly found in this silo.
     *
     * Warning: the content is re-scanned each time, not cached.
     *
     * @return the block type mainly found in this silo.
     */
    public BlockType getMainItem()
    {
        BlockType main = null;
        Integer amount = -1;

        for (Map.Entry<BlockType, Integer> item : getContent().entrySet())
        {
            if (item.getValue() > amount)
            {
                amount = item.getValue();
                main = item.getKey();
            }
        }

        return main;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Silo silo = (Silo) o;

        return !(mainOutput != null ? !mainOutput.equals(silo.mainOutput) : silo.mainOutput != null) && containers.equals(silo.containers);
    }

    @Override
    public int hashCode()
    {
        return 31 * (mainOutput != null ? mainOutput.hashCode() : 0) + containers.hashCode();
    }

    @Override
    public String toString()
    {
        return "Silo with " + getContainers().size() + " containers"
                + (getMainOutput() != null ? " starting at " + getMainOutput() : "");
    }
}
