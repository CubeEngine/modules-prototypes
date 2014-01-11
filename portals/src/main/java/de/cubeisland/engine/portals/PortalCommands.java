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
package de.cubeisland.engine.portals;

import java.io.File;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.service.Selector;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.portals.config.Destination;
import de.cubeisland.engine.portals.config.PortalConfig;

public class PortalCommands extends ContainerCommand
{
    private Portals module;
    private PortalManager manager;

    public PortalCommands(Portals module, PortalManager manager)
    {
        super(module, "portals", "The portal commands", Arrays.asList("mvp"));
        this.module = module;
        this.manager = manager;
    }

    @Alias(names = "mvpc")
    @Command(desc = "Creates a new Portal" , usage = "<name> [worlddest <world>]|[portaldest <portal>]", min = 1, max = 1,
    params = {
        @Param(names = "worlddest", completer = WorldCompleter.class, type = World.class),
        @Param(names = "portaldest")
    })
    public void create(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            Selector selector = this.getModule().getCore().getModuleManager().getServiceManager().getServiceImplementation(Selector.class);
            User sender = (User)context.getSender();
            if (selector.getSelection(sender) instanceof Cuboid)
            {
                if (this.manager.getPortal(context.getString(0)) == null)
                {
                    Location p1 = selector.getFirstPoint(sender);
                    Location p2 = selector.getSecondPoint(sender);
                    PortalConfig config = this.getModule().getCore().getConfigFactory().create(PortalConfig.class);
                    config.location.from = new BlockVector3(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ());
                    config.location.to = new BlockVector3(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
                    config.location.destination = new WorldLocation(sender.getLocation());
                    config.owner = sender.getOfflinePlayer();
                    config.world = new ConfigWorld(module.getCore().getWorldManager(), p1.getWorld());
                    if (context.hasParam("worlddest"))
                    {
                        World world = context.getParam("worlddest", null);
                        if (world == null)
                        {
                            context.sendTranslated("&cWorld &6%s&c not found!", context.getString("worlddest"));
                            return;
                        }
                        config.destination = new Destination(world);
                    }
                    if (context.hasParam("portaldest"))
                    {
                        Portal portal = this.manager.getPortal(context.getString("portaldest"));
                        if (portal == null)
                        {
                            context.sendTranslated("&cPortal &6%s&c not found!", context.getString("portaldest"));
                            return;
                        }
                        config.destination = new Destination(portal);
                    }
                    config.setFile(new File(manager.portalsDir, context.getString(0) + ".yml"));
                    config.save();
                    Portal portal = new Portal(module, manager, context.getString(0), config);
                    this.manager.addPortal(portal);
                    sender.attachOrGet(PortalsAttachment.class, module).setPortal(portal);
                    context.sendTranslated("&aPortal &6%s&a created! Select a destination using portal modify destination command", portal.getName());
                    return;
                }
                context.sendTranslated("&cA portal named &6%s&c already exists!", context.getString(0));
            }
            else
            {
                context.sendTranslated("&cPlease select a cuboid first!");
            }
            return;
        }
        context.sendTranslated("&cYou have to be ingame to do this!");
    }

    @Alias(names = "mvps")
    @Command(desc = "Selects an existing portal" , usage = "<name>", min = 1, max = 1)
    public void select(CommandContext context)
    {
        Portal portal = this.manager.getPortal(context.getString(0));
        if (portal == null)
        {
            context.sendTranslated("&cThere is no portal named &6%s", context.getString(0));
            return;
        }
        if (context.getSender() instanceof User)
        {
            ((User)context.getSender()).attachOrGet(PortalsAttachment.class, module).setPortal(portal);
            context.sendTranslated("&aPortal selected: &6%s", context.getString(0));
        }
        context.sendTranslated("&cYou have to be ingame to do this!");
    }

    public void info()
    {
        // TODO
    }

    @Alias(names = "mvpr")
    @Command(desc = "Removes a portal permanently", min = 1, max = 1, usage = "<portal>")
    public void remove(CommandContext context)
    {
        Portal portal = this.manager.getPortal(context.getString(0));
        if (portal == null)
        {
            context.sendTranslated("&cThere is no portal named &6%s", context.getString(0));
            return;
        }
        portal.delete();
        context.sendTranslated("&aPortal &6%s&a deleted", portal.getName());
    }

    @Command(desc = "Shows debug portal information instead of teleporting", usage = "on|off", max = 1, min = 1)
    public void debug(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            ((User)context.getSender()).attachOrGet(PortalsAttachment.class, module).toggleDebug();
            return;
        }
        context.sendTranslated("&cYou have to be ingame to do this!");
    }
}
