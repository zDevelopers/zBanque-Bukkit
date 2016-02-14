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
package fr.zcraft.zbanque.gui;

import fr.zcraft.zbanque.Permissions;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.utils.NumberUtils;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class BankGUI extends ExplorerGui<Map.Entry<BlockType, Long>>
{
    private final Bank bank;

    public BankGUI(Bank bank)
    {
        this.bank = bank;
    }

    @Override
    protected void onUpdate()
    {
        final Set<Map.Entry<BlockType, Long>> bankContent = bank.getOrderedContent(true);

        setTitle(I.t("{black}{0} {reset}({1})", bank.getDisplayName(), bank.getTotalItemsCount()));
        setData(bankContent.toArray(new Map.Entry[bankContent.size()]));

        setMode(Mode.READONLY);
        setKeepHorizontalScrollingSpace(true);

        if (Permissions.SEE_STRUCTURE.isGrantedTo(getPlayer()))
        {
            action("structure", getSize() - 5, GuiUtils.makeItem(
                    Material.CHEST,
                    I.t("{green}{bold}See the bank structure"),
                    I.tn("{white}{0}{gray} silo", "{white}{0}{gray} silos", bank.getSilos().size())
            ));
        }
    }

    @Override
    protected ItemStack getViewItem(Map.Entry<BlockType, Long> content)
    {
        BlockType type = content.getKey();
        Long amount = content.getValue();

        return GuiUtils.makeItem(
                new ItemStack(type.getType(), Math.max(NumberUtils.long2int(amount / 1000), 1), type.getData()),
                null,
                Collections.singletonList(
                        I.tn("{white}{0}{gray} item stored", "{white}{0}{gray} items stored", NumberUtils.long2int(amount), amount)
                )
        );
    }

    @GuiAction ("structure")
    protected void structure()
    {
        Gui.open(getPlayer(), new BankStructureGUI(bank), this);
    }
}
