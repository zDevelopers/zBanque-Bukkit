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
import fr.zcraft.zbanque.commands.shortcuts.WithBankNameCommand;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.containers.Silo;
import fr.zcraft.zbanque.utils.LocationUtils;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.Set;


@CommandInfo (name = "structure", usageParameters = "<bank>")
public class StructureViewCommand extends WithBankNameCommand
{
    @Override
    protected void run() throws CommandException
    {
        final Bank bank = getBankFromArgs(0);
        final Set<Silo> structure = bank.getSilos();

        if (structure.isEmpty())
        {
            info(I.t("This bank is empty."));
            return;
        }

        final String header = I.tn("{gold}{bold}{0} silo in the {1} bank", "{gold}{bold}{0} silos in the {1} bank", structure.size(), structure.size(), bank.getDisplayName());
        info(header);

        for (Silo silo : structure)
        {
            final Integer chestsCount = silo.getContainers().size();
            info(I.tn("{bold}Silo at {0} {gray}({1} chest)", "{bold}Silo at {0} {gray}({1} chests)", chestsCount, silo.getMainOutput() != null ? LocationUtils.userFriendlyLocation(silo.getMainOutput().getMainLocation()) : I.t("(unknown)"), chestsCount));

            final Map<BlockType, Integer> content = silo.getContent();
            if (content.isEmpty())
                info(I.t("{italic}   (empty)"));

            for (Map.Entry<BlockType, Integer> stack : content.entrySet())
            {
                info(I.tn("{darkgray}- {gray}{0} {darkgray}»{white} {1} {gray}item", "{darkgray}- {gray}{0} {darkgray}»{white} {1} {gray}items", stack.getValue(), stack.getKey(), stack.getValue()));
            }
        }

        info(header);
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
            return autocompleteForBank(0);

        else return null;
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.SEE_STRUCTURE.isGrantedTo(sender);
    }
}
