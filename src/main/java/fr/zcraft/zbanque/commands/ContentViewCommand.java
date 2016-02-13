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
import fr.zcraft.zbanque.commands.mixins.WithBankNameCommandMixin;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.utils.NumberUtils;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunAsyncTask;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@CommandInfo (name = "content", usageParameters = "<bank> [reverse]")
public class ContentViewCommand extends WithBankNameCommandMixin
{
    @Override
    protected void run() throws CommandException
    {
        final CommandSender sender = this.sender;
        final Bank bank = getBankFromArgs(0);
        final boolean reverse = args.length > 1 && args[1].equalsIgnoreCase("reverse");

        RunAsyncTask.nextTick(new Runnable()
        {
            @Override
            public void run()
            {
                for (Map.Entry<BlockType, Long> item : bank.getOrderedContent(reverse))
                {
                    sender.sendMessage(I.tn("{white}{0} {gray}item of {1}", "{white}{0} {gray}items of {1}", NumberUtils.long2int(item.getValue()), item.getValue(), item.getKey()));
                }
            }
        });
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
            return autocompleteForBank(0);

        else if (args.length == 2)
            return getMatchingSubset(Collections.singletonList("reverse"), args[1]);

        else return null;
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.SEE_CONTENT.isGrantedTo(sender);
    }
}
