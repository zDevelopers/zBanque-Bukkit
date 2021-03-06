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
import fr.zcraft.zbanque.utils.NumberUtils;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.GlowEffect;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class BanksGUI extends ExplorerGui<Bank>
{
    private final List<Bank> banks;

    public BanksGUI(List<Bank> banks)
    {
        this.banks = banks;
    }

    @Override
    protected void onUpdate()
    {
        setTitle(I.t("{black}Banks {reset}({0})", banks.size()));
        setData(banks.toArray(new Bank[banks.size()]));

        setMode(Mode.READONLY);
        setKeepHorizontalScrollingSpace(true);

        if (Permissions.SEE_ITEMS_GROUPS.isGrantedTo(getPlayer()))
        {
            action("items_groups", getSize() - 5, new ItemStackBuilder(Material.BOOK)
                    .title(I.t("{green}Grouped stocks overview"))
            );
        }
    }

    @Override
    protected ItemStack getViewItem(Bank bank)
    {
        final Long totalItemsCount = bank.getTotalItemsCount();

        ItemStack item = GuiUtils.makeItem(
                Material.CHEST,
                // TRANSLATORS: The name of a bank, in the banks list GUI.
                I.t("{green}{bold}{0}", bank.getDisplayName()),

                // TRANSLATORS: The codename of a bank, in the banks list GUI.
                I.t("{gray}{0}", bank.getCodeName()),
                "",
                I.tn("{white}{0}{gray} item stored", "{white}{0}{gray} items stored", NumberUtils.long2int(totalItemsCount), totalItemsCount),
                I.tn("{white}{0}{gray} silo", "{white}{0}{gray} silos", bank.getSilos().size()),
                "",
                I.t("{darkgray}» {white}Click{gray} to open this bank")
        );

        GlowEffect.addGlow(item);

        return item;
    }

    @Override
    protected ItemStack getEmptyViewItem()
    {
        return new ItemStackBuilder(Material.BARRIER)
                .title(ChatColor.RED, I.t("No bank created"))
                .longLore(ChatColor.GRAY, I.t("If you are the server owner, checkout the banks section of the configuration file to create banks."))
                .hideAttributes()
                .item();
    }

    @Override
    protected ItemStack getPickedUpItem(Bank bank)
    {
        onRightClick(bank);
        return null;
    }

    @Override
    protected void onRightClick(Bank bank)
    {
        Gui.open(getPlayer(), new BankGUI(bank), this);
    }

    @GuiAction ("items_groups")
    protected void groups()
    {
        Gui.open(getPlayer(), new ItemsGroupsGUI(banks), this);
    }
}
