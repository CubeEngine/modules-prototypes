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
package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.*;

public class Fun extends Module
{
    private FunConfiguration config;
    private FunPerm perm;

    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(FunResource.values());
        this.perm = new FunPerm(this);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommands(this, new ThrowCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new NukeCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new PlayerCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new DiscoCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new InvasionCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new RocketCommand(this), ReflectedCommand.class);
    }

    @Override
    public void onDisable()
    {
        this.perm.cleanup();
    }

    public FunConfiguration getConfig()
    {
        return this.config;
    }
}