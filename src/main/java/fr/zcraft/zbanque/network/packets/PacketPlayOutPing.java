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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.zcraft.zbanque.Config;
import fr.zcraft.zbanque.ZBanque;
import fr.zcraft.zlib.tools.PluginLogger;


public class PacketPlayOutPing extends PacketPlayOut
{
    public PacketPlayOutPing()
    {
        setEndpoint("/ping");
        setPacketType(PacketType.GET);
    }

    @Override
    public void onResponse(JsonElement data)
    {
        boolean invalidResponse = false;

        if (!data.isJsonObject())
            invalidResponse = true;

        JsonObject object = data.getAsJsonObject();
        if (object.get("version") == null || !object.get("version").isJsonPrimitive())
            invalidResponse = true;

        if (invalidResponse)
        {
            PluginLogger.error("Strange response received from the /ping endpoint: {0}", data);
            PluginLogger.warning("Cannot check the webservice version, the networking system may not work!");
            return;
        }

        String version = object.getAsJsonPrimitive("version").getAsString();

        PluginLogger.info("WebService contacted successfully - remote version: {0}", version);
        if (!version.startsWith(ZBanque.WEBSERVICE_COMPATIBLE_VERSION))
            PluginLogger.warning("Remote major version is not the same as the plugin version, errors may occur.");

        ZBanque.get().setWebServiceEnabled(true);
    }

    @Override
    public void onError(Throwable exception)
    {
        PluginLogger.error("Cannot contact the webservice at {0} - the network operations will be disabled", Config.WEBSERVICE.URL.get());
        PluginLogger.error("Error received: {0}: {1}", exception.getClass().getSimpleName(), exception.getMessage());
        ZBanque.get().setWebServiceEnabled(false);
    }
}
