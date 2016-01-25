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
package org.cubeengine.module.powersigns.storage;

import org.cubeengine.module.powersigns.signtype.SignTypeInfo;
import org.cubeengine.service.database.AsyncRecord;
import de.cubeisland.engine.module.core.storage.database.AsyncRecord;
import org.spongepowered.api.world.Location;
import org.jooq.types.UInteger;

public class PowerSignModel extends AsyncRecord<PowerSignModel>
{
    public PowerSignModel()
    {
        super(TablePowerSign.TABLE_POWER_SIGN);
    }

    public PowerSignModel newPSign(SignTypeInfo info)
    {
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.OWNER_ID, UInteger.valueOf(info.getCreator()));
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.PSID, info.getType().getPSID());
        Location location = info.getLocation();
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.WORLD, info.getWorldID());
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.X, location.getBlockX());
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.Y, location.getBlockY());
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.Z, location.getBlockZ());
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.CHUNKX, location.getChunk().getX());
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.CHUNKZ, location.getChunk().getZ());
        this.setValue(TablePowerSign.TABLE_POWER_SIGN.DATA, info.serializeData());
        return this;
    }
}
