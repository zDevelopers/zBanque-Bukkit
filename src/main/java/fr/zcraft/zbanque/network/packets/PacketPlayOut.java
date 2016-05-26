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
import fr.zcraft.zbanque.Config;
import fr.zcraft.zbanque.network.PacketSender;
import fr.zcraft.zbanque.network.HTTPResponse;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public abstract class PacketPlayOut
{
    public enum PacketType { GET, POST }

    private String endpoint = null;
    private PacketType type = PacketType.POST;
    private JsonElement data = null;

    private HTTPResponse httpResponse = null;

    private Set<Callback<JsonElement>> successCallbacks = new CopyOnWriteArraySet<>();
    private Set<Callback<Throwable>> errorsCallbacks = new CopyOnWriteArraySet<>();


    /**
     * @return The API endpoint for this packet, starting with a "/".
     */
    public String getEndpoint()
    {
        return endpoint;
    }

    /**
     * @param endpoint The API endpoint for this packet.
     */
    public void setEndpoint(final String endpoint)
    {
        this.endpoint = endpoint;

        if (!this.endpoint.startsWith("/"))
            this.endpoint = "/" + this.endpoint;
    }

    /**
     * @return The JSON data to be sent.
     */
    public JsonElement getData()
    {
        return data;
    }

    /**
     * @param data The JSON data to be sent.
     */
    public void setData(JsonElement data)
    {
        this.data = data;
    }

    public PacketType getPacketType()
    {
        return type;
    }

    public void setPacketType(PacketType type)
    {
        this.type = type;
    }

    /**
     * @param callback A callback executed if the packet is successfully sent. The callback receives the WebService response.
     */
    public PacketPlayOut addSuccessCallback(Callback<JsonElement> callback)
    {
        successCallbacks.add(callback);
        return this;
    }

    /**
     * @param callback A callback executed if the packet fails to be sent. The callback receives the exception thrown.
     */
    public PacketPlayOut addErrorCallback(Callback<Throwable> callback)
    {
        errorsCallbacks.add(callback);
        return this;
    }

    /**
     * @return The callbacks to execute when the packet is successfully sent. The callback receives the WebService response.
     */
    public Set<Callback<JsonElement>> getSuccessCallbacks()
    {
        return successCallbacks;
    }

    /**
     * @return The callbacks to execute when the packet fails to be sent. The callback receives the exception thrown.
     */
    public Set<Callback<Throwable>> getErrorsCallbacks()
    {
        return errorsCallbacks;
    }

    /**
     * @return The raw HTTP response. Only populated when the request is sent, {@code null} before.
     */
    public HTTPResponse getHttpResponse()
    {
        return httpResponse;
    }

    /**
     * Sets the HTTP response. Internal use.
     *
     * @param httpResponse The HTTP response.
     */
    public void setHttpResponse(HTTPResponse httpResponse)
    {
        this.httpResponse = httpResponse;
    }


    /**
     * Called when a response is received from the server.
     *
     * <p>Warning: this is called from the packets thread, out of the main Bukkit thread!</p>
     */
    public void onResponse(JsonElement data) {}

    /**
     * Called if the packet cannot be delivered.
     *
     * <p>Warning: this is called from the packets thread, out of the main Bukkit thread!</p>
     */
    public void onError(Throwable exception)
    {
        PluginLogger.error("Error while sending a {0} packet", exception, this.getClass().getSimpleName());
    }


    /**
     * Sends this packet.
     */
    public void send()
    {
        if (!Config.WEBSERVICE.URL.isDefined() || Config.WEBSERVICE.URL.get().isEmpty())
            return;

        if (getEndpoint() == null)
            throw new IllegalStateException("Cannot send an uninitialized packet");

        PacketSender.sendPacket(this);
    }
}
