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

import org.bukkit.permissions.Permissible;


public enum Permissions
{
    SEE_CONTENT("zbanque.see-content"),
    SEE_STRUCTURE("zbanque.see-structure"),
    UPDATE_STRUCTURE("zbanque.update-structure");


    private final String permission;

    Permissions(String permission)
    {
        this.permission = permission;
    }


    /**
     * @return the permission's name.
     */
    public String getPermission()
    {
        return permission;
    }

    /**
     * Checks if this permission is granted to the given permissible.
     *
     * @param permissible The permissible to check.
     *
     * @return {@code true} if this permission is granted to the permissible.
     */
    public boolean isGrantedTo(Permissible permissible)
    {
        return permissible.hasPermission(permission);
    }
}


