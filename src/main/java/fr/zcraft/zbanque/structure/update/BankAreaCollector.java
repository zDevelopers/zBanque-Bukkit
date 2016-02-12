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
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunAsyncTask;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;


public class BankAreaCollector extends BukkitRunnable
{
    private final CommandSender requestedBy;
    private final boolean verbose;

    private final Bank bank;

    private int layer;
    private int loadedBlocks = 0;

    private final Area area;

    public BankAreaCollector(Bank bank, CommandSender requestedBy, boolean verbose)
    {
        this.requestedBy = requestedBy;
        this.verbose = verbose;

        requestedBy.sendMessage(I.t("{gray}Loading bank area..."));

        this.bank = bank;

        this.layer = bank.getLowestCorner().getBlockX();
        this.area = new Area(bank);
    }

    @Override
    public void run()
    {
        if (verbose)
            requestedBy.sendMessage(I.t("{gray}{italic}Loading layer at x = {0}...", layer));

        final int yMin = bank.getLowestCorner().getBlockY();
        final int zMin = bank.getLowestCorner().getBlockZ();
        final int yMax = bank.getHighestCorner().getBlockY();
        final int zMax = bank.getHighestCorner().getBlockZ();

        final World world = bank.getLowestCorner().getWorld();

        int loadedBlocks = 0;

        for (int y = yMin; y <= yMax; y++)
        {
            for (int z = zMin; z <= zMax; z++)
            {
                final Block block = world.getBlockAt(layer, y, z);
                area.set(layer, y, z, new BlockType(block.getType(), block.getData()));
                loadedBlocks++;
            }
        }

        this.loadedBlocks += loadedBlocks;

        if (layer == bank.getHighestCorner().getBlockX())
        {
            if (verbose)
                requestedBy.sendMessage(I.tn("{gray}Bank area loaded ({0} block).", "{gray}Bank area loaded ({0} blocks).", this.loadedBlocks));

            RunAsyncTask.later(new BankAnalyzer(bank, area, requestedBy, verbose), 20l);
            cancel();
        }
        else
        {
            layer++;
        }
    }
}
