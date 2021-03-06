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

import fr.zcraft.zbanque.structure.BanksManager;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.items_groups.ItemsGroup;
import fr.zcraft.zbanque.structure.items_groups.ItemsGroupManager;
import fr.zcraft.zbanque.utils.Pair;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class ItemsGroupsGUI extends ExplorerGui<Map.Entry<ItemsGroup, Pair<Long, Map<BlockType, Long>>>>
{
    private final List<Bank> banks;

    public ItemsGroupsGUI(List<Bank> banks)
    {
        this.banks = banks;
    }

    @Override
    protected void onUpdate()
    {
        final Set<Map.Entry<ItemsGroup, Pair<Long, Map<BlockType, Long>>>> entries = ItemsGroupManager.get().getGlobalAmounts(BanksManager.get().getBanks()).entrySet();

        setTitle(I.t("{black}Grouped stocks overview"));
        setMode(Mode.READONLY);
        setData(entries.toArray(new Map.Entry[entries.size()]));
        setKeepHorizontalScrollingSpace(true);

        action("back", getSize() - 5, Material.EMERALD, I.t("{green}Go back"));
    }

    @Override
    protected ItemStack getViewItem(Map.Entry<ItemsGroup, Pair<Long, Map<BlockType, Long>>> data)
    {
        return ItemsGroup.asRepresentingItem(data);
    }

    @Override
    protected ItemStack getEmptyViewItem()
    {
        return new ItemStackBuilder(Material.BARRIER)
                .title(ChatColor.RED, I.t("There isn't any group to display"))
                .longLore(ChatColor.GRAY, I.t("If you are the server owner, checkout the groups section of the configuration file to create groups."))
                .hideAttributes()
                .item();
    }

    @GuiAction ("back")
    protected void back()
    {
        close();
    }
}
