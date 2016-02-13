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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import fr.zcraft.zbanque.ZBanque;
import fr.zcraft.zbanque.json.ContainerTypeAdapter;
import fr.zcraft.zbanque.json.LocationTypeAdapter;
import fr.zcraft.zbanque.structure.update.BankAreaCollector;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;


public class Bank
{
    private final transient String codeName;
    private final transient String displayName;

    private final transient Location lowestCorner;
    private final transient Location highestCorner;

    private final Set<Silo> silos = new CopyOnWriteArraySet<>();


    /**
     * @param codeName    A code name for the bank (must be unique).
     * @param displayName The bank's display name.
     * @param corner1     A corner of the bank area.
     * @param corner2     The opposite corner of the bank area.
     */
    public Bank(final String codeName, final String displayName, final Location corner1, final Location corner2)
    {
        this.codeName = codeName;
        this.displayName = displayName;

        this.highestCorner = new Location(
                corner1.getWorld(),
                Math.max(corner1.getX(), corner2.getX()),
                Math.min(Math.max(corner1.getY(), corner2.getY()), corner1.getWorld().getMaxHeight()),
                Math.max(corner1.getZ(), corner2.getZ())
        );

        this.lowestCorner = new Location(
                corner1.getWorld(),
                Math.min(corner1.getX(), corner2.getX()),
                Math.max(Math.min(corner1.getY(), corner2.getY()), 0),
                Math.min(corner1.getZ(), corner2.getZ())
        );
    }

    /**
     * @return The bank code name.
     */
    public String getCodeName()
    {
        return codeName;
    }

    /**
     * @return The bank display name.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * @return The corner of the bank area with the lowest coordinates.
     */
    public Location getLowestCorner()
    {
        return lowestCorner;
    }

    /**
     * @return The corner of the bank area with the highest coordinates.
     */
    public Location getHighestCorner()
    {
        return highestCorner;
    }

    /**
     * Retrieves the silos of this bank. A silo is a set of chests and hoppers
     * linked together.
     *
     * @return The silos in this bank.
     */
    public Set<Silo> getSilos()
    {
        return Collections.unmodifiableSet(silos);
    }

    /**
     * @return The content of the bank: a map associating a {@link BlockType}
     * with the amount of items of this type in the bank.
     */
    public Map<BlockType, Long> getContent()
    {
        Map<BlockType, Long> bankContent = new HashMap<>();
        for (Silo silo : getSilos())
        {
            for (Map.Entry<BlockType, Integer> content : silo.getContent().entrySet())
            {
                Long value = bankContent.containsKey(content.getKey()) ? bankContent.get(content.getKey()) : 0l;
                value += content.getValue();

                bankContent.put(content.getKey(), value);
            }
        }

        return bankContent;
    }

    /**
     * @param reverseOrder If {@code true}, orders the content by reverse order
     *                     (lowest last).
     *
     * @return The content of the bank, ordered by amount.
     * @see #getContent()
     */
    public Set<Map.Entry<BlockType, Long>> getOrderedContent(Boolean reverseOrder)
    {
        final int reverse = reverseOrder ? -1 : 1;

        Set<Map.Entry<BlockType, Long>> bankContentSorted = new TreeSet<>(new Comparator<Map.Entry<BlockType, Long>>()
        {
            @Override
            public int compare(Map.Entry<BlockType, Long> one, Map.Entry<BlockType, Long> other)
            {
                if (one.getValue() < other.getValue())
                    return -1 * reverse;
                else if (one.getValue() > other.getValue())
                    return reverse;
                else
                    return 0;
            }
        });

        bankContentSorted.addAll(getContent().entrySet());
        return bankContentSorted;
    }

    /**
     * @return The total amount of items in all types in the bank.
     */
    public Long getTotalItemsCount()
    {
        Long total = 0l;

        for (Long amount : getContent().values())
            total += amount;

        return total;
    }

    /**
     * @param silo The silo to add in the bank.
     */
    public void addSilo(Silo silo)
    {
        silos.add(silo);
    }

    /**
     * @param silo The silo to remove from the bank.
     */
    public void removeSilo(Silo silo)
    {
        silos.remove(silo);
    }


    /**
     * Scans and updates the bank structure.
     *
     * @param requestedBy The requester of this update. Messages related to the
     *                    update process will be sent to this user.
     * @param verbose     If {@code true}, a verbose log will be sent.
     */
    public void updateStructure(CommandSender requestedBy, boolean verbose)
    {
        requestedBy.sendMessage(I.t("{cst}{bold}Starting bank structure update for {0}.", getDisplayName()));
        RunTask.timer(new BankAreaCollector(this, requestedBy, verbose), 1l, 1l);
    }

    /**
     * Save the bank in a file. This should be called asynchronously.
     *
     * <p>Banks are saved in the {@code plugins/zBanque/banks/<code-name>.json}
     * file. Only the structure and content are saved, as the names and corners
     * are in the config file.</p>
     */
    public void saveToFile()
    {
        Gson exporter = getGson();
        String jsonExport = exporter.toJson(this);

        File bankFile = getStorageFile();
        if (bankFile == null)
            return;

        FileWriter out = null;
        try
        {
            out = new FileWriter(bankFile);
            out.write(jsonExport);
            out.flush();
        }
        catch (IOException e)
        {
            PluginLogger.error("Error while writing the {0} bank content to the {1} file", e, getCodeName(), bankFile.getAbsolutePath());
        }
        finally
        {
            try
            {
                if (out != null)
                    out.close();
            }
            catch (IOException e)
            {
                PluginLogger.error("Error while closing the {0} file", e, bankFile.getAbsolutePath());
            }
        }
    }

    /**
     * Loads the bank from a previously saved file. This should be called
     * asynchronously.
     *
     * <p>Banks are loaded from the {@code plugins/zBanque/banks/<code-name>.json}
     * file.</p>
     */
    public void loadFromFile()
    {
        Gson importer = getGson();

        File bankFile = getStorageFile();
        if (bankFile == null)
            return;

        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new FileReader(bankFile));

            final Bank importedBank = importer.fromJson(in, getClass());
            if (importedBank != null)
            {
                silos.clear();
                silos.addAll(importedBank.getSilos());
            }
        }
        catch (JsonParseException e)
        {
            PluginLogger.error("Unable to parse JSON to import the {0} bank", e, getCodeName());
        }
        catch (IOException e)
        {
            PluginLogger.error("Error while reading the {0} bank content to the {1} file", e, getCodeName(), bankFile.getAbsolutePath());
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException e)
            {
                PluginLogger.error("Error while closing the {0} file", e, bankFile.getAbsolutePath());
            }
        }
    }

    /**
     * @return A new {@link Gson} instance used to import or export the data.
     */
    private Gson getGson()
    {
        return new GsonBuilder()
                .registerTypeAdapter(Container.class, new ContainerTypeAdapter())
                .registerTypeAdapter(Location.class, new LocationTypeAdapter())
                .create();
    }

    /**
     * @return The file were this bank will be stored.
     */
    private File getStorageFile()
    {
        File banksDirectory = new File(ZBanque.get().getDataFolder(), "banks");
        if (!banksDirectory.exists() && !banksDirectory.mkdirs())
        {
            PluginLogger.error("Cannot create the banks data folder at {0}", banksDirectory.getAbsolutePath());
            return null;
        }

        if (!banksDirectory.isDirectory())
        {
            PluginLogger.error("Cannot create the banks data folder at {0}, as a file with the same name already exists!", banksDirectory.getAbsolutePath());
            return null;
        }

        File storageFile = new File(banksDirectory, getCodeName() + ".json");
        try
        {
            storageFile.createNewFile();
        }
        catch (IOException e)
        {
            PluginLogger.error("Error while creating the bank storing file for {0} at {1}", e, getCodeName(), storageFile.getAbsolutePath());
        }

        return storageFile;
    }
}
