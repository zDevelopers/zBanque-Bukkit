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
     * @param data The block data value. If -1, any data will match (wildcard data value) in {@link
     *             #equals(Object) the equals method}.
     */
    public BlockType(Material type, short data)
    {
        Validate.notNull(type, "The block type cannot be null");

        this.type = type;
        this.data = data;
    }

    /**
     * Data-wildcard constructor. Any data value will match in {@link #equals(Object) the equals
     * method}.
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
     * @param data The block data value. If -1, any data will match (wildcard data value) in {@link
     *             #equals(Object) the equals method}.
     */
    public void setData(short data)
    {
        this.data = data;
    }


    @Override
    public String toString()
    {
        return type.name() + ":" + data;
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
        return 31 * type.hashCode() + (int) data;
    }
}
