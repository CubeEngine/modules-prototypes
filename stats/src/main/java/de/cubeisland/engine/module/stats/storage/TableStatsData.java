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

import java.sql.Timestamp;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.module.stats.storage.TableStats.TABLE_STATS;
import static org.jooq.impl.SQLDataType.*;

public class TableStatsData extends AutoIncrementTable<StatsDataModel, UInteger>
{
    public static TableStatsData TABLE_STATSDATA;

    public final TableField<StatsDataModel, UInteger> KEY = createField("key", INTEGERUNSIGNED, this);
    public final TableField<StatsDataModel, UInteger> STAT = createField("stat", INTEGERUNSIGNED, this);
    public final TableField<StatsDataModel, Timestamp> TIME = createField("timestamp", TIMESTAMP, this);
    public final TableField<StatsDataModel, String> DATA = createField("data", VARCHAR.length(64), this);


    private TableStatsData(String prefix)
    {
        super(prefix + "statsdata", new Version(1));
        setAIKey(KEY);
        addForeignKey(TABLE_STATS.getPrimaryKey(), STAT);
        addFields(KEY, STAT, TIME, DATA);
    }

    @Override
    public Class<StatsDataModel> getRecordType()
    {
        return StatsDataModel.class;
    }
}
