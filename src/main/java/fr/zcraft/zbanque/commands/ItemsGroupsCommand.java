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
package fr.zcraft.zbanque.commands;

import fr.zcraft.zbanque.Permissions;
import fr.zcraft.zbanque.structure.BanksManager;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.items_groups.ItemsGroup;
import fr.zcraft.zbanque.structure.items_groups.ItemsGroupManager;
import fr.zcraft.zbanque.utils.Pair;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;


@CommandInfo (name = "groups")
public class ItemsGroupsCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        info(I.t("{blue}{bold}Grouped items"));

        for (Map.Entry<ItemsGroup, Pair<Long, Map<BlockType, Long>>> entry : ItemsGroupManager.get().getGlobalAmounts(BanksManager.get().getBanks()).entrySet())
        {
            send(new RawText("- ")
                        .then(entry.getValue().getLeft().toString())
                            .color(ChatColor.GREEN)

                        .then(" × ")
                            .color(ChatColor.DARK_GREEN)

                        .then(entry.getKey().getTitle())
                            .color(ChatColor.GREEN)
                            .hover(ItemsGroup.asRepresentingItem(entry))

                    .build()
            );
        }
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.SEE_ITEMS_GROUPS.isGrantedTo(sender);
    }
}
