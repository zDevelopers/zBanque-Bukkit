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

import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.structure.items_groups.ItemsGroup;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationList;
import fr.zcraft.zlib.components.configuration.ConfigurationMap;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import org.bukkit.World;
import org.bukkit.util.Vector;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.*;


public final class Config
{
    public final static ConfigurationMap<String, BankSection> BANKS = map("banks", String.class, BankSection.class);

    public final static class BankSection extends ConfigurationSection
    {
        public final ConfigurationItem<String> NAME = item("name", String.class);

        public final ConfigurationItem<World> WORLD = item("world", World.class);
        public final ConfigurationItem<Vector> FIRST_CORNER = item("firstCorner", Vector.class);
        public final ConfigurationItem<Vector> OTHER_CORNER = item("otherCorner", Vector.class);

        public final ConfigurationItem<Vector> CENTER = item("center", Vector.class);
    }

    public final static ConfigurationList<ItemsGroup> GROUPS = list("groups", ItemsGroup.class);

    public final static WebserviceSection WEBSERVICE = section("webservice", WebserviceSection.class);

    public final static class WebserviceSection extends ConfigurationSection
    {
        public final ConfigurationItem<String> URL = item("url", "");
        public final ConfigurationItem<String> USERNAME = item("username", "");
        public final ConfigurationItem<String> PASSWORD = item("password", "");
    }


    static
    {
        ConfigurationValueHandlers.registerHandlers(ItemsGroup.class);
        ConfigurationValueHandlers.registerHandlers(BlockType.class);
    }
}
