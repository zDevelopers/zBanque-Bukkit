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
package fr.zcraft.zbanque.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.containers.Container;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Type;
import java.util.Map;


public class ContainerTypeAdapter implements JsonDeserializer<Container>
{
    @Override
    public Container deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
    {
        final JsonObject rawContainer = jsonElement.getAsJsonObject();

        final Location main = jsonDeserializationContext.deserialize(rawContainer.get("mainLocation"), Location.class);
        Location secondary = null;

        final JsonElement rawSecondaryLocation = rawContainer.get("secondaryLocation");
        if (rawSecondaryLocation != null)
            secondary = jsonDeserializationContext.deserialize(rawSecondaryLocation, Location.class);

        final Container container = new Container(main, secondary, true);

        final JsonObject content = rawContainer.getAsJsonObject("content");
        for (Map.Entry<String, JsonElement> entry : content.entrySet())
        {
            final String[] rawType = entry.getKey().split(":");
            if (rawType.length != 2)
            {
                PluginLogger.error("Malformed JSON content: malformed item type {0} in {1}", entry.getKey(), jsonElement.toString());
                continue;
            }

            final Material itemType = Material.matchMaterial(rawType[0]);
            if (itemType == null)
            {
                PluginLogger.error("Malformed JSON content: unknown item type {0} in {1}", rawType[0], jsonElement.toString());
                continue;
            }

            final Short itemData;
            try
            {
                itemData = Short.valueOf(rawType[1]);
            }
            catch (NumberFormatException e)
            {
                PluginLogger.error("Malformed JSON content: badly formatted data value {0} in {1}", e, rawType[1], jsonElement.toString());
                continue;
            }

            BlockType blockType = new BlockType(itemType, itemData);
            Integer amount = entry.getValue().getAsInt();

            container.updateBlockType(blockType, amount);
        }

        final JsonElement rawContainerType = rawContainer.get("containerType");
        if (rawContainerType != null)
            container.setContainerType(Material.matchMaterial(rawContainerType.getAsString()));

        PluginLogger.info("Retrieved container type: {0} (raw: {1})", container.getContainerType(), rawContainerType);

        return container;
    }
}
