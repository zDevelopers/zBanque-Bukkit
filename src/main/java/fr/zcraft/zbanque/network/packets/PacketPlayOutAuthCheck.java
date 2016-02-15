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


public class PacketPlayOutAuthCheck extends PacketPlayOut
{
    public PacketPlayOutAuthCheck()
    {
        setEndpoint("/check_auth");
        setPacketType(PacketType.POST);
    }

    @Override
    public void onResponse(JsonElement data)
    {
        boolean invalidResponse = false;

        if (!data.isJsonObject())
            invalidResponse = true;

        JsonObject object = data.getAsJsonObject();

        if ((!object.has("username") || !object.get("username").isJsonPrimitive()) || (!object.has("permissions") || !object.get("permissions").isJsonObject()))
            invalidResponse = true;

        if (invalidResponse)
        {
            PluginLogger.error("Strange response received from the /check_auth endpoint: {0}", data);
            PluginLogger.warning("You are authenticated, but the permissions cannot be checked. Requests may fail!");
            return;
        }

        final JsonObject permissions = object.get("permissions").getAsJsonObject();

        final String username = object.get("username").getAsString();
        final Boolean canPost = isPermissionGranted(permissions, "can_post");

        PluginLogger.info("Authentication successful against the webservice as {0}", username);

        if (!canPost)
        {
            PluginLogger.error("The user {0} is not allowed to send POST request. Disabling webservice integration.");
            ZBanque.get().setWebServiceEnabled(false);
        }
        else
        {
            ZBanque.get().setWebServiceEnabled(true);
        }
    }

    @Override
    public void onError(Throwable exception)
    {
        PluginLogger.error("Unable to check authentication permissions, are you sure the credentials are valid?");
        PluginLogger.error("Username provided: {0}", Config.WEBSERVICE_USERNAME.get());
        PluginLogger.error("The network operations will be disabled", exception, Config.WEBSERVICE_URL.get());

        ZBanque.get().setWebServiceEnabled(false);
    }

    private boolean isPermissionGranted(JsonObject permissions, String permission)
    {
        return permissions.has(permission) && permissions.get(permission).isJsonPrimitive() && permissions.get(permission).getAsBoolean();
    }
}
