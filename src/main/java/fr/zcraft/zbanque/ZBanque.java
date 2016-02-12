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

import fr.zcraft.zbanque.commands.ContentViewCommand;
import fr.zcraft.zbanque.commands.ListBanksCommand;
import fr.zcraft.zbanque.commands.StructureUpdateCommand;
import fr.zcraft.zbanque.commands.StructureViewCommand;
import fr.zcraft.zbanque.structure.BanksManager;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.core.ZPlugin;

import java.util.Locale;


public class ZBanque extends ZPlugin
{
    private static ZBanque INSTANCE;

    @SuppressWarnings ("unchecked")
    @Override
    public void onEnable()
    {
        INSTANCE = this;

        loadComponents(I18n.class, Commands.class);

        I18n.useDefaultPrimaryLocale();
        I18n.setFallbackLocale(Locale.US);

        saveDefaultConfig();

        Commands.register(
                "zbanque",
                ListBanksCommand.class,
                ContentViewCommand.class,
                StructureViewCommand.class,
                StructureUpdateCommand.class
        );

        BanksManager.get().registerBanksInConfig(getConfig().getConfigurationSection("banks"));
    }

    public static ZBanque get()
    {
        return INSTANCE;
    }
}
