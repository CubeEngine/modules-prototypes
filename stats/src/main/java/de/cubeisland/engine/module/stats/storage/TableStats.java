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
package de.cubeisland.engine.module.stats.storage;

import de.cubeisland.engine.module.core.storage.database.AutoIncrementTable;
import org.cubeengine.module.core.util.Version;
import org.jooq.TableField;
import org.jooq.types.UInteger;

import static org.jooq.impl.SQLDataType.VARCHAR;

public class TableStats extends AutoIncrementTable<StatsModel, UInteger>
{
    public static TableStats TABLE_STATS;
    public final TableField<StatsModel, UInteger> KEY = createField("key", U_INTEGER.length(10), this);
    public final TableField<StatsModel, String> STAT = createField("stat", VARCHAR.length(64), this);

    public TableStats(String prefix)
    {
        super(prefix + "stats", new Version(1));
        setAIKey(KEY);
        addFields(KEY, STAT);
        TABLE_STATS = this;
    }

    @Override
    public Class<StatsModel> getRecordType()
    {
        return StatsModel.class;
    }
}
