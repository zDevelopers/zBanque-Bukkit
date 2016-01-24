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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * A set of chests linked together.
 *
 * <p>This is the main storage used here. A single chest is represented by a silo with a single
 * chest inside.</p>
 *
 * @see Chest The storage units.
 */
public class Silo
{
    /**
     * The main output of the silo, where users take the resources.
     */
    private Chest mainOutput;

    /**
     * The chests contained in this silo.
     */
    private final Set<Chest> chests = new CopyOnWriteArraySet<>();


    public Chest getMainOutput()
    {
        return mainOutput;
    }

    public void setMainOutput(Chest mainOutput)
    {
        this.mainOutput = mainOutput;
    }

    public Set<Chest> getChests()
    {
        return chests;
    }

    public void addChest(Chest chest)
    {
        chests.add(chest);
    }

    public void removeChest(Chest chest)
    {
        chests.remove(chest);
    }
}
