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
package fr.zcraft.zbanque.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.containers.Container;
import fr.zcraft.zbanque.structure.containers.Silo;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class PacketPlayOutSilos extends PacketPlayOut
{
    public PacketPlayOutSilos(Bank bank, Set<Silo> silos)
    {
        setEndpoint("/silos");
        setPacketType(PacketType.POST);

        JsonArray silosJson = new JsonArray();
        for (Silo silo : silos)
            silosJson.add(siloToWSJson(bank, silo));

        setData(silosJson);
    }

    public PacketPlayOutSilos(Bank bank)
    {
        this(bank, bank.getSilos());
    }

    public PacketPlayOutSilos(Bank bank, Silo silo)
    {
        this(bank, Collections.singleton(silo));
    }

    private JsonElement siloToWSJson(Bank bank, Silo silo)
    {
        JsonObject json = new JsonObject();

        json.add("banque", bank2WSBank(bank));
        json.add("itemPrincipal", item2WSItem(silo.getMainItem()));

        json.addProperty("x", silo.getMainOutput().getMainLocation().getX());
        json.addProperty("z", silo.getMainOutput().getMainLocation().getZ());

        JsonArray containersJson = new JsonArray();

        for (Container container : silo.getContainers())
        {
            JsonObject containerJson = new JsonObject();

            containerJson.addProperty("map", container.getMainLocation().getWorld().getName());
            containerJson.addProperty("x", container.getMainLocation().getBlockX());
            containerJson.addProperty("y", container.getMainLocation().getBlockY());
            containerJson.addProperty("z", container.getMainLocation().getBlockZ());

            JsonArray itemStacksJson = new JsonArray();

            for (Map.Entry<BlockType, Integer> item : container.getContent().entrySet())
            {
                JsonObject itemStackJson = new JsonObject();

                itemStackJson.add("item", item2WSItem(item.getKey()));
                itemStackJson.addProperty("montant", item.getValue());

                itemStacksJson.add(itemStackJson);
            }

            containerJson.add("itemStacks", itemStacksJson);

            containersJson.add(containerJson);
        }

        json.add("coffres", containersJson);

        return json;
    }

    private JsonElement bank2WSBank(Bank bank)
    {
        JsonObject bankJson = new JsonObject();

        bankJson.addProperty("nom", bank.getCodeName());

        bankJson.addProperty("x", bank.getCenter().getBlockX());
        bankJson.addProperty("y", bank.getCenter().getBlockY());
        bankJson.addProperty("z", bank.getCenter().getBlockZ());

        bankJson.addProperty("map", bank.getCenter().getWorld().getName());

        return bankJson;
    }

    private JsonElement item2WSItem(BlockType item)
    {
        if (item == null)
            return JsonNull.INSTANCE;

        JsonObject itemJson = new JsonObject();

        itemJson.addProperty("idItem", item.getType().getId());
        itemJson.addProperty("data", item.getType().getMaxDurability());

        return itemJson;
    }
}
