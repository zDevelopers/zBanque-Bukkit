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
package fr.zcraft.zbanque;

import com.google.gson.JsonElement;
import fr.zcraft.zbanque.commands.ContentViewCommand;
import fr.zcraft.zbanque.commands.ExploreCommand;
import fr.zcraft.zbanque.commands.ItemsGroupsCommand;
import fr.zcraft.zbanque.commands.ListBanksCommand;
import fr.zcraft.zbanque.commands.StructureUpdateCommand;
import fr.zcraft.zbanque.commands.StructureViewCommand;
import fr.zcraft.zbanque.network.PacketSender;
import fr.zcraft.zbanque.network.packets.PacketPlayOutAuthCheck;
import fr.zcraft.zbanque.network.packets.PacketPlayOutPing;
import fr.zcraft.zbanque.structure.BanksManager;
import fr.zcraft.zbanque.structure.items_groups.ItemsGroupManager;
import fr.zcraft.zbanque.structure.update.BankContentLiveUpdater;
import fr.zcraft.zbanque.utils.AsyncAccess;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.core.ZPlugin;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;

import java.util.Locale;


public class ZBanque extends ZPlugin
{
    public static final String WEBSERVICE_COMPATIBLE_VERSION = "1.";

    private static ZBanque INSTANCE;
    private boolean webServiceEnabled = true;

    public static ZBanque get()
    {
        return INSTANCE;
    }

    @SuppressWarnings ("unchecked")
    @Override
    public void onEnable()
    {
        INSTANCE = this;

        loadComponents(
                I18n.class, Commands.class, Gui.class, PacketSender.class, AsyncAccess.class,
                BanksManager.class, BankContentLiveUpdater.class, ItemsGroupManager.class
        );

        I18n.useDefaultPrimaryLocale();
        I18n.setFallbackLocale(Locale.US);

        saveDefaultConfig();
        Configuration.init(Config.class);

        Commands.register(
                "zbanque",
                ExploreCommand.class,
                ListBanksCommand.class,
                ContentViewCommand.class,
                StructureViewCommand.class,
                StructureUpdateCommand.class,
                ItemsGroupsCommand.class
        );

        Commands.registerShortcut("zbanque", ExploreCommand.class, "banque");

        enableWebService();
    }

    @Override
    public void onDisable()
    {
        BanksManager.get().saveAll();
        super.onDisable();
    }

    private void enableWebService()
    {
        if (!Config.WEBSERVICE.URL.isDefined() || Config.WEBSERVICE.URL.get().isEmpty())
        {
            PluginLogger.info("The webservice integration is disabled in the configuration.");
            setWebServiceEnabled(false);
            return;
        }
        else if (!Config.WEBSERVICE.URL.get().toLowerCase().startsWith("http"))
        {
            PluginLogger.error("Non-HTTP or HTTPS webservice URLs are currently not supported.");
            setWebServiceEnabled(false);
            return;
        }

        final PacketPlayOutPing ping = new PacketPlayOutPing();

        ping.addSuccessCallback(new Callback<JsonElement>()
        {
            @Override
            public void call(JsonElement parameter)
            {
                new PacketPlayOutAuthCheck().send();
            }
        });

        ping.send();
    }

    public boolean isWebServiceEnabled()
    {
        return webServiceEnabled;
    }

    public void setWebServiceEnabled(boolean webServiceEnabled)
    {
        this.webServiceEnabled = webServiceEnabled;
    }
}
