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
package org.cubeengine.module.powersigns;

import de.cubeisland.engine.module.core.module.Module;
import org.cubeengine.module.powersigns.storage.TablePowerSign;

public class Powersigns extends Module
{
    private PowersignsConfig config;
    private SignManager signManager;

    @Override
    public void onEnable()
    {
        this.getCore().getDB().registerTable(TablePowerSign.class);
        this.config = this.loadConfig(PowersignsConfig.class);
        this.signManager = new SignManager(this);
        this.signManager.init();
    }

    public SignManager getManager()
    {
        return signManager;
    }
}