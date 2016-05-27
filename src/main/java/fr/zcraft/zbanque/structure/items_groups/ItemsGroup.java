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
package fr.zcraft.zbanque.structure.items_groups;

import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.utils.Pair;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ItemsGroup
{
    private String title;
    private BlockType icon;
    private Map<BlockType, Double> relativeAmounts = new HashMap<>();

    public ItemsGroup(String title, BlockType icon)
    {
        Validate.notNull(title, "The title cannot be null");
        Validate.notNull(icon, "The icon cannot be null");

        this.title = title;
        this.icon = icon;
    }

    public String getTitle()
    {
        return title;
    }

    public BlockType getIcon()
    {
        return icon;
    }

    public void registerRelativeAmount(BlockType type, Double relativeAmount)
    {
        relativeAmounts.put(type, relativeAmount);
    }

    public Map<BlockType, Double> getRelativeAmounts()
    {
        return Collections.unmodifiableMap(relativeAmounts);
    }

    public Double getRelativeAmountFor(BlockType type)
    {
        return relativeAmounts.get(type);
    }

    public Pair<Long, Map<BlockType, Long>> getAmounts(final Collection<Bank> banks)
    {
        Long amount = 0l;
        Map<BlockType, Long> detail = new HashMap<>();

        for (Bank bank : banks)
        {
            for (Map.Entry<BlockType, Long> entry : bank.getContent().entrySet())
            {
                final BlockType type = entry.getKey();
                if (relativeAmounts.containsKey(type))
                {
                    amount += (long) Math.ceil(relativeAmounts.get(type) * entry.getValue());
                    detail.put(type, entry.getValue() + (detail.containsKey(type) ? detail.get(type) : 0));
                }
            }
        }

        return new Pair<>(amount, detail);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemsGroup that = (ItemsGroup) o;

        return title.equals(that.title) && icon == that.icon && relativeAmounts.equals(that.relativeAmounts);

    }

    @Override
    public int hashCode()
    {
        int result = title.hashCode();
        result = 31 * result + icon.hashCode();
        result = 31 * result + relativeAmounts.hashCode();
        return result;
    }


    public static ItemStack asRepresentingItem(Map.Entry<ItemsGroup, Pair<Long, Map<BlockType, Long>>> itemsGroupDetailedEntry)
    {
        final ItemsGroup itemsGroup = itemsGroupDetailedEntry.getKey();
        final BlockType icon = itemsGroup.getIcon();

        ItemStackBuilder item = new ItemStackBuilder(icon.getType())
                .data((short) Math.max(icon.getData(), 0))
                .title(ChatColor.GREEN, itemsGroup.getTitle())
                .loreLine(I.tn("{white}{0}{gray} item", "{white}{0}{gray} items", itemsGroupDetailedEntry.getValue().getLeft().intValue()))
                .loreLine()
                .loreLine(I.t("{blue}Including..."));

        for (Map.Entry<BlockType, Long> detail : itemsGroupDetailedEntry.getValue().getRight().entrySet())
        {
            item.loreLine(
                    I.tn("{gray}- {white}{0}{gray} item of {1} {darkgray}(× {2})", "{white}{0}{gray} items of {1} {darkgray}(× {2})",
                            detail.getValue().intValue(), detail.getValue(), detail.getKey().toUserString(),
                            itemsGroup.getRelativeAmountFor(detail.getKey()))
            );
        }

        return item.item();
    }

    @ConfigurationValueHandler
    public static ItemsGroup handleItemsGroup(Map map) throws ConfigurationParseException
    {
        if (!map.containsKey("name"))
            throw new ConfigurationParseException("Key 'name' required.", map);

        if (!map.containsKey("icon"))
            throw new ConfigurationParseException("Key 'icon' required.", map);

        final String name = map.get("name").toString();
        final BlockType icon = BlockType.handleBlockType(map.get("icon"));

        final ItemsGroup group = new ItemsGroup(name, icon);

        if (map.containsKey("relative_amounts"))
        {
            for (Map.Entry<BlockType, Double> entry : ConfigurationValueHandlers.handleMapValue(map.get("relative_amounts"), BlockType.class, Double.class).entrySet())
            {
                group.registerRelativeAmount(entry.getKey(), entry.getValue());
            }
        }

        return group;
    }
}
