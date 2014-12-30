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
package de.cubeisland.engine.social;

import de.cubeisland.engine.command.DispatcherCommand;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.social.interactions.SocialCommand;
import de.cubeisland.engine.social.interactions.SocialListener;
import de.cubeisland.engine.social.interactions.SocialSubCommand;
import de.cubeisland.engine.social.sites.facebook.FacebookManager;

public class Social extends Module
{
    public FacebookManager facebookManager;
    private SocialConfig config;

    @Override
    public void onEnable()
    {
        this.config = loadConfig(SocialConfig.class);
        this.facebookManager = new FacebookManager(config);

        if (!this.facebookManager.initialize())
        {
            this.getLog().error("Facebook could not be initialized. The module is shutting down.");
            this.getCore().getModuleManager().disableModule(this);
            return;
        }

        CommandManager cm = this.getCore().getCommandManager();
        cm.addCommands(cm, this, new SocialCommand(this));
        cm.addCommands((DispatcherCommand)cm.getCommand("facebook"), this, new SocialSubCommand(this));
        this.getCore().getEventManager().registerListener(this, new SocialListener(this));
    }

    public FacebookManager getFacebookManager()
    {
        return facebookManager;
    }
}
