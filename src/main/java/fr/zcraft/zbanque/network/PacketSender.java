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
package fr.zcraft.zbanque.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.zcraft.zbanque.Config;
import fr.zcraft.zbanque.ZBanque;
import fr.zcraft.zbanque.network.packets.PacketPlayOut;
import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerAttributes;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@WorkerAttributes (name = "zbanque-networking-out")
public class PacketSender extends Worker
{
    private final static String USER_AGENT = "zBanqueBukkit/" + ZBanque.get().getDescription().getVersion();

    /**
     * Sends a packet.
     *
     * @param packet The packet to send.
     */
    public static void sendPacket(final PacketPlayOut packet)
    {
        sendPacket(packet, new WorkerCallback<JsonElement>() {
            @Override
            public void finished(JsonElement result)
            {
                if (packet.getHttpResponse().getResponseCode() == 401)
                {
                    PluginLogger.error("Request to {0} failed: authentication needed", packet.getEndpoint());
                    return;
                }

                if (result != null)
                {
                    packet.onResponse(result);

                    for (Callback<JsonElement> callback : packet.getSuccessCallbacks())
                    {
                        try
                        {
                            callback.call(result);
                        }
                        catch (Exception e)
                        {
                            PluginLogger.error("Uncaught exception thrown by a {0} success callback", e, packet.getClass().getSimpleName());
                        }
                    }
                }
            }

            @Override
            public void errored(Throwable exception)
            {
                if (exception instanceof JsonParseException)
                {
                    PluginLogger.error("The packet {0} received an invalid JSON response {1} (HTTP code {2})", packet.getClass().getSimpleName(), packet.getHttpResponse().getBody(), packet.getHttpResponse().getResponseCode());
                    return;
                }

                packet.onError(exception);

                for (Callback<Throwable> callback : packet.getErrorsCallbacks())
                {
                    try
                    {
                        callback.call(exception);
                    }
                    catch (Exception e)
                    {
                        PluginLogger.error("Uncaught exception thrown by a {0} error callback", e, packet.getClass().getSimpleName());
                    }
                }
            }
        });
    }

    public static void sendPacket(final PacketPlayOut packet, final WorkerCallback<JsonElement> callback)
    {
        submitQuery(new WorkerRunnable<JsonElement>() {
            @Override
            public JsonElement run() throws Throwable
            {
                PluginLogger.info("Sending packet");
                if (!ZBanque.get().isWebServiceEnabled())
                    return null;
                PluginLogger.info("Sending packet - enabled");

                final String url = Config.WEBSERVICE_URL.get() + packet.getEndpoint();
                final String data = packet.getData() == null ? null : packet.getData().toString();

                final HTTPResponse response = makeRequest(url, packet.getPacketType(), data);
                packet.setHttpResponse(response);

                return new JsonParser().parse(response.getBody());
            }
        }, callback);
    }

    private static HTTPResponse makeRequest(String url, PacketPlayOut.PacketType method, String data) throws Throwable
    {
        PluginLogger.info("Request method: {0}", method);
        // ***  REQUEST  ***

        final URL urlObj = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

        connection.setRequestMethod(method.name());
        connection.setRequestProperty("User-Agent", USER_AGENT);

        connection.setDoOutput(true);

        if (method == PacketPlayOut.PacketType.POST)
        {
            DataOutputStream out = null;
            try
            {
                out = new DataOutputStream(connection.getOutputStream());
                if (data != null)
                    out.writeBytes(data);
                out.flush();
            }
            finally
            {
                if (out != null)
                    out.close();
            }
        }



        // ***  RESPONSE  ***


        int responseCode = connection.getResponseCode();

        BufferedReader in = null;
        String body = "";
        try
        {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null)
            {
                responseBuilder.append(inputLine);
            }

            body = responseBuilder.toString();
        }
        finally
        {
            if (in != null)
                in.close();
        }

        HTTPResponse response = new HTTPResponse();
        response.setResponseCode(responseCode);
        response.setResponseBody(body);

        int i = 0;
        String headerName, headerContent;
        while ((headerName = connection.getHeaderFieldKey(i)) != null)
        {
            headerContent = connection.getHeaderField(i);
            response.addHeader(headerName, headerContent);
        }


        // ***  REDIRECTION  ***

        switch (responseCode)
        {
            case 301:
            case 302:
            case 307:
            case 308:
                if (response.getHeaders().containsKey("Location"))
                {
                    response = makeRequest(response.getHeaders().get("Location"), method, data);
                }
        }



        // ***  END  ***

        return response;
    }
}
