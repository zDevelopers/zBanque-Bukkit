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
package fr.zcraft.zbanque.structure.containers;

import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


/**
 * An item type.
 */
public class BlockType
{
    private Material type;
    private short data;

    /**
     * @param type The block type. Cannot be {@code null}.
     * @param data The block data value. If -1, any data will match (wildcard
     *             data value) in {@link #equals(Object) the equals method}.
     */
    public BlockType(Material type, short data)
    {
        Validate.notNull(type, "The block type cannot be null");

        this.type = type;
        this.data = keepDurability(type) ? data : (short) 0;
    }

    /**
     * Data-wildcard constructor. Any data value will match in {@link
     * #equals(Object) the equals method}.
     *
     * @param type The block type. Cannot be {@code null}.
     */
    public BlockType(Material type)
    {
        this(type, (short) -1);
    }

    public BlockType(ItemStack stack)
    {
        this(stack.getType(), stack.getDurability());
    }

    public Material getType()
    {
        return type;
    }

    public void setType(Material type)
    {
        this.type = type;
    }

    public short getData()
    {
        return data;
    }

    /**
     * @param data The block data value. If -1, any data will match (wildcard
     *             data value) in {@link #equals(Object) the equals method}.
     */
    public void setData(short data)
    {
        this.data = data;
    }


    /**
     * @param material Material
     *
     * @return {@code true} if the durability has to be kept while using this as
     * a key to differentiate materials. As example, the durability is not kept
     * for tools (where durability is the damages), maps...
     */
    private boolean keepDurability(Material material)
    {
        switch (material)
        {
            case WOOD_SWORD:
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:

            case STONE_SWORD:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SPADE:

            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:

            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:

            case IRON_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SPADE:
            case IRON_BARDING:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:

            case GOLD_SWORD:
            case GOLD_AXE:
            case GOLD_HOE:
            case GOLD_PICKAXE:
            case GOLD_SPADE:
            case GOLD_BARDING:
            case GOLD_HELMET:
            case GOLD_CHESTPLATE:
            case GOLD_LEGGINGS:
            case GOLD_BOOTS:

            case DIAMOND_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SPADE:
            case DIAMOND_BARDING:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:

            case MAP:
            case WRITTEN_BOOK:

            case SHEARS:
            case ELYTRA:
            case FLINT_AND_STEEL:
            case SHIELD:
            case BOW:
                return false;

            default:
                return true;
        }
    }


    @Override
    public String toString()
    {
        return type.name() + ":" + data;
    }

    public String toUserString()
    {
        return type.name().replace("_", " ") + (data > 0 ? ":" + data : "");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockType blockType = (BlockType) o;

        return (data == -1 || blockType.data == -1 || data == blockType.data) && type == blockType.type;
    }

    @Override
    public int hashCode()
    {
        return 31 * type.hashCode();
    }


    @ConfigurationValueHandler
    public static BlockType handleBlockType(Object object) throws ConfigurationParseException
    {
        try
        {
            return new BlockType(ConfigurationValueHandlers.handleEnumValue(object, Material.class));
        }
        catch (ConfigurationParseException e)
        {
            final String str = object.toString().trim();

            if (str.contains(":"))
            {
                String[] parts = str.split(":");
                short data;

                try { data = Short.parseShort(parts[1]); }
                catch (NumberFormatException nfe)
                {
                    throw new ConfigurationParseException("Invalid block type, must be a Material, Material:DATAVALUE or Material_DATAVALUE", object);
                }

                return new BlockType(ConfigurationValueHandlers.handleEnumValue(parts[0], Material.class), data);
            }
            else
            {
                String[] parts = str.split("_");

                String name = StringUtils.join(parts, "_", 0, parts.length - 1);
                short data;

                try { data = Short.parseShort(parts[parts.length - 1]); }
                catch (NumberFormatException nfe)
                {
                    throw new ConfigurationParseException("Invalid block type, must be a Material, Material:DATAVALUE or Material_DATAVALUE", object);
                }

                return new BlockType(ConfigurationValueHandlers.handleEnumValue(name, Material.class), data);
            }
        }
    }
}
