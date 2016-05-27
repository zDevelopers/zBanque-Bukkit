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
package fr.zcraft.zbanque.structure.items_groups;

import fr.zcraft.zbanque.Config;
import fr.zcraft.zbanque.structure.containers.Bank;
import fr.zcraft.zbanque.structure.containers.BlockType;
import fr.zcraft.zbanque.utils.Pair;
import fr.zcraft.zlib.core.ZLibComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ItemsGroupManager extends ZLibComponent
{
    private static ItemsGroupManager INSTANCE;

    private List<ItemsGroup> itemGroups = new ArrayList<>();

    @Override
    protected void onEnable()
    {
        INSTANCE = this;

        registerGroupsFromConfig();
    }

    public static ItemsGroupManager get()
    {
        return INSTANCE;
    }

    public void registerGroup(ItemsGroup group)
    {
        itemGroups.add(group);
    }

    public List<ItemsGroup> registeredGroups()
    {
        return Collections.unmodifiableList(itemGroups);
    }

    private void registerGroupsFromConfig()
    {
        for (ItemsGroup group : Config.GROUPS)
            registerGroup(group);
    }


    public Map<ItemsGroup, Pair<Long, Map<BlockType, Long>>> getGlobalAmounts(Bank bank)
    {
        return getGlobalAmounts(Collections.singletonList(bank));
    }

    public Map<ItemsGroup, Pair<Long, Map<BlockType, Long>>> getGlobalAmounts(List<Bank> banks)
    {
        // LinkedHashMap to keep the insertion order so the config order is kept in the UIs.
        Map<ItemsGroup, Pair<Long, Map<BlockType, Long>>> groupsWithAmounts = new LinkedHashMap<>();

        for (ItemsGroup group : itemGroups)
        {
            groupsWithAmounts.put(group, group.getAmounts(banks));
        }

        return groupsWithAmounts;
    }
}
