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
import fr.zcraft.zbanque.structure.containers.Container;
import fr.zcraft.zbanque.structure.containers.Silo;
import fr.zcraft.zbanque.utils.LocationUtils;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class BankStructureSiloGUI extends ExplorerGui<Container>
{
    private final Bank bank;
    private final Silo silo;

    public BankStructureSiloGUI(Bank bank, Silo silo)
    {
        this.bank = bank;
        this.silo = silo;
    }

    @Override
    protected void onUpdate()
    {
        final Set<Container> containers = silo.getContainers();

        setTitle(I.t("{0} » {black}Silo {reset}({1})", bank.getDisplayName(), containers.size()));
        setData(containers.toArray(new Container[containers.size()]));
        setMode(Mode.READONLY);
    }

    @Override
    protected ItemStack getViewItem(Container container)
    {
        int itemsInside = 0;
        for (int amount : container.getContent().values())
            itemsInside += amount;

        ItemStack item = GuiUtils.makeItem(
                container.getContainerType() != null ? container.getContainerType() : Material.BARRIER,
                container.getContainerType() != null ? null : I.t("{ce}INVALID"),
                "",
                I.t("{gray}Main location: {0}", LocationUtils.userFriendlyLocation(container.getMainLocation())),
                I.t("{gray}Secondary location: {0}", container.getSecondaryLocation() != null ? LocationUtils.userFriendlyLocation(container.getSecondaryLocation()) : I.tc("gui_container_location", "none")),
                "",
                I.t("{blue}Content {gray}({0})", itemsInside)
        );

        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = meta.getLore();

        if (container.getContent() == null || container.getContent().isEmpty())
            lore.add(I.t("{gray}{italic}(empty)"));
        else
            for (Map.Entry<BlockType, Integer> content : container.getContent().entrySet())
                lore.add(I.tn("{white}{0}{gray} item of {1}:{2}", "{white}{0}{gray} items of {1}", content.getValue(), content.getValue(), content.getKey().getType(), content.getKey().getData()));

        if (Permissions.TP_TO_CHEST.isGrantedTo(getPlayer()) && container.getMainLocation() != null)
        {
            lore.add("");
            lore.add(I.t("{darkgray}» {white}Click{gray} to teleport yourself to this chest"));
            lore.add(I.t("{darkred}WARNING! {red}Teleportation to the exact chest location."));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected ItemStack getEmptyViewItem()
    {
        return new ItemStackBuilder(Material.BARRIER)
                .title(ChatColor.RED, I.t("There isn't any chest in this silo"))
                .longLore(ChatColor.GRAY, I.t("Put some chests and/or rerun /zbanque update-structure {0}, and they will be displayed here.", bank.getCodeName()))
                .hideAttributes()
                .item();
    }

    @Override
    protected ItemStack getPickedUpItem(Container container)
    {
        onRightClick(container);
        return null;
    }

    @Override
    protected void onRightClick(Container container)
    {
        if (Permissions.TP_TO_CHEST.isGrantedTo(getPlayer()) && container.getMainLocation() != null)
            getPlayer().teleport(container.getMainLocation());
    }
}
