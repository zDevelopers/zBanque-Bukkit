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
package fr.zcraft.zbanque.containers;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;


/**
 * Stores the block types of an area.
 */
public class Area
{
    /**
     * The data.
     *
     * <p>The block indexes are shifted by an offset, so all indexes are positive.<br />The shift is
     * the distance from 0 to the minimal coordinate, so the lowest is always at the index 0.</p>
     */
    private final BlockType[][][] area;

    private final int xOffset;
    private final int yOffset;
    private final int zOffset;

    private final Location lowestCorner;
    private final Location highestCorner;

    /**
     * The lowest & highest coordinates are used to allocate the storage memory and to calculate
     * indexes offsets.
     *
     * @param lowestCorner  The corner with the <strong>lowest</strong> coordinates.
     * @param highestCorner The corner with the <strong>highest</strong> coordinates.
     */
    public Area(Location lowestCorner, Location highestCorner)
    {
        this.area = new BlockType[highestCorner.getBlockX() - lowestCorner.getBlockX() + 1][highestCorner.getBlockY() - lowestCorner.getBlockY() + 1][highestCorner.getBlockZ() - lowestCorner.getBlockZ() + 1];

        this.xOffset = -lowestCorner.getBlockX();
        this.yOffset = -lowestCorner.getBlockY();
        this.zOffset = -lowestCorner.getBlockZ();

        this.lowestCorner = lowestCorner;
        this.highestCorner = highestCorner;
    }

    /**
     * Sets a block type.
     *
     * @param x    X coordinate.
     * @param y    Y coordinate.
     * @param z    Z coordinate.
     * @param type Block type.
     *
     * @throws IllegalArgumentException if the location is out of the area.
     */
    public void set(int x, int y, int z, BlockType type)
    {
        try
        {
            area[xOffset + x][yOffset + y][zOffset + z] = type;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException("The location at " + x + ";" + y + ";" + z + " cannot be stored inside this area", e);
        }
    }

    /**
     * Sets a block type.
     *
     * @param vector Coordinates.
     * @param type   Block type.
     *
     * @throws IllegalArgumentException if the location is out of the area.
     */
    public void set(Vector vector, BlockType type)
    {
        set(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ(), type);
    }

    /**
     * Gets a block type.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     *
     * @throws IllegalArgumentException if the location is out of the area.
     */
    public BlockType get(int x, int y, int z)
    {
        try
        {
            return area[xOffset + x][yOffset + y][zOffset + z];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException("The location at " + x + ";" + y + ";" + z + " cannot be retrieved inside this area", e);
        }
    }

    /**
     * Gets a block type.
     *
     * @param vector Coordinates.
     *
     * @throws IllegalArgumentException if the location is out of the area.
     */
    public BlockType get(Vector vector)
    {
        return get(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Returns the block type next to the given one, following the {@code face} direction.
     *
     * <p>Only straight, diagonal and self directions are supported. Others (like north-north-east)
     * will behave like {@link BlockFace#SELF}.</p>
     *
     * @param x    X coordinate.
     * @param y    Y coordinate.
     * @param z    Z coordinate.
     * @param face Direction.
     *
     * @return The block type.
     * @throws IllegalArgumentException if the returned location is out of the area.
     */
    public BlockType getNextTo(int x, int y, int z, BlockFace face)
    {
        return get(getLocationNextTo(x, y, z, face));
    }

    /**
     * Returns the block type next to the given one, following the {@code face} direction.
     *
     * <p>Only straight, diagonal and self directions are supported. Others (like north-north-east)
     * will behave like {@link BlockFace#SELF}.</p>
     *
     * @param vector Coordinates.
     * @param face   Direction.
     *
     * @return The block type.
     * @throws IllegalArgumentException if the returned location is out of the area.
     */
    public BlockType getNextTo(Vector vector, BlockFace face)
    {
        return get(getLocationNextTo(vector, face));
    }

    /**
     * Returns the location next to the given one, following the {@code face} direction.
     *
     * <p>Only straight, diagonal and self directions are supported. Others (like north-north-east)
     * will behave like {@link BlockFace#SELF}.</p>
     *
     * @param x    X coordinate.
     * @param y    Y coordinate.
     * @param z    Z coordinate.
     * @param face Direction.
     *
     * @return The location.
     */
    public Vector getLocationNextTo(int x, int y, int z, BlockFace face)
    {
        switch (face)
        {
            case NORTH:
                z--;
                break;

            case EAST:
                x++;
                break;

            case SOUTH:
                z++;
                break;

            case WEST:
                x--;
                break;

            case UP:
                y++;
                break;

            case DOWN:
                y--;
                break;

            case NORTH_EAST:
                z--;
                x++;
                break;

            case NORTH_WEST:
                z--;
                x--;
                break;

            case SOUTH_EAST:
                z++;
                x++;
                break;

            case SOUTH_WEST:
                z++;
                x--;
                break;
        }

        return new Vector(x, y, z);
    }

    /**
     * Returns the block type next to the given one, following the {@code face} direction.
     *
     * <p>Only straight, diagonal and self directions are supported. Others (like north-north-east)
     * will behave like {@link BlockFace#SELF}.</p>
     *
     * @param vector Coordinates.
     * @param face   Direction.
     *
     * @return The location.
     */
    public Vector getLocationNextTo(Vector vector, BlockFace face)
    {
        return getLocationNextTo(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ(), face);
    }


    /**
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     *
     * @return {@code true} if inside this area.
     */
    public boolean isInside(int x, int y, int z)
    {
        return x >= lowestCorner.getBlockX() && x <= highestCorner.getBlockX()
                && y >= lowestCorner.getBlockY() && y <= highestCorner.getBlockY()
                && z >= lowestCorner.getBlockZ() && z <= highestCorner.getBlockZ();
    }

    /**
     * @param vector A vector.
     *
     * @return {@code true} if inside this area.
     */
    public boolean isInside(Vector vector)
    {
        return isInside(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }


    /**
     * @return The raw data internally stored.
     */
    public BlockType[][][] getRawData()
    {
        return area;
    }

    /**
     * @return The lowest corner of this area.
     */
    public Location getLowestCorner()
    {
        return lowestCorner;
    }

    /**
     * @return The highest corner of this area.
     */
    public Location getHighestCorner()
    {
        return highestCorner;
    }

}
