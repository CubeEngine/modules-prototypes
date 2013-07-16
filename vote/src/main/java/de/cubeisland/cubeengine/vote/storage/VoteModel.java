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
package de.cubeisland.cubeengine.vote.storage;

import java.sql.Timestamp;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;


@SingleKeyEntity(tableName = "votes", primaryKey = "userid", autoIncrement = false, indices = {
    @Index(value = FOREIGN_KEY, fields = "userid", f_table = "user", f_field = "key")
})
public class VoteModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long userid;
    @Attribute(type = AttrType.TIMESTAMP)
    public Timestamp lastvote;
    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public int voteamount;

    // Database-constructor
    public VoteModel()
    {}

    public VoteModel(User user)
    {
        this.userid = user.key;
        this.lastvote = new Timestamp(System.currentTimeMillis());
        this.voteamount = 1;
    }

    @Override
    public Long getId()
    {
        return this.userid;
    }

    @Override
    public void setId(Long aLong)
    {
        throw new UnsupportedOperationException();
    }
}
