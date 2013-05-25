/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.log.storage;

import java.util.HashSet;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.action.ActionType;
import de.cubeisland.cubeengine.log.action.ActionType.Category;


public class Lookup implements Cloneable
{
    private final Log module;

    private QueryParameter queryParameter;
    private QueryResults queryResults;

    private Lookup(Log module)
    {
        this.module = module;
    }

    /**
     * Lookup excluding nothing
     * @return
     */
    public static Lookup general(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(new HashSet<ActionType>(), false); // exclude none
        return lookup;
    }
    /**
     * Lookup only including container-actions
     */
    public static Lookup container(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.INVENTORY.getActionTypes(), true); // include inv
        return lookup;
    }

    /**
     * Lookup only including kill-actions
     */
    public static Lookup kills(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.KILL.getActionTypes(), true); // include kils
        return lookup;
    }

    /**
     * Lookup only including player-actions
     */
    public static Lookup player(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.PLAYER.getActionTypes(), true); // include player
        return lookup;
    }

    /**
     * Lookup only including block-actions
     */
    public static Lookup block(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.BLOCK.getActionTypes(), true); // include block
        return lookup;
    }

    public void show(User user, int page)
    {
        LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
        attachment.setCommandLookup(this);
        this.queryResults.show(user,queryParameter,page);
    }

    public void setQueryResults(QueryResults queryResults)
    {
        this.queryResults = queryResults;
    }

    public QueryParameter getQueryParameter()
    {
        return this.queryParameter;
    }

    @Override
    public Lookup clone()
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = queryParameter.clone();
        return lookup;
    }

    public boolean queried()
    {
        return this.queryResults != null;
    }

    public void rollback(User user)
    {
        LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
        attachment.setCommandLookup(this);
        this.queryResults.rollback(user);
    }
}
