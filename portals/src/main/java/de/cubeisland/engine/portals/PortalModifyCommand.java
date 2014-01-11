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

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.service.Selector;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.portals.config.Destination;

public class PortalModifyCommand extends ContainerCommand
{
    private PortalManager manager;

    public PortalModifyCommand(Portals module, PortalManager manager)
    {
        super(module, "modify", "modifies a portal");
        this.registerAlias(new String[]{"mvpm"}, new String[0]);
        this.manager = manager;
    }

    @Command(desc = "Changes the owner of a portal",
    usage = "<owner> [portal]", min = 1, max = 2)
    public void owner(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s&c not found!", context.getString(0));
            return;
        }
        Portal portal = null;
        if (context.hasArg(1))
        {
            portal = manager.getPortal(context.getString(1));
            if (portal == null)
            {
                context.sendTranslated("&cPortal &6%s&c not found!", context.getString(1));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated("&cYou need to define a portal to use!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        portal.config.owner = user.getOfflinePlayer();
        portal.config.save();
        context.sendTranslated("&2%s&a is now the owner of &6%s&a!", user.getName(), portal.getName());
    }

    @Alias(names = "mvpd")
    @Command(names = {"destination","dest"},
        desc = "changes the destination of the selected portal",
             usage = "here|<world>|<p:<portal>> [p <portal>]", min = 1, max = 1,
    params = @Param(names = {"p","portal"}))
    public void destination(ParameterizedContext context)
    {
        Portal portal = null;
        if (context.hasParam("p"))
        {
            portal = manager.getPortal(context.getString("p"));
            if (portal == null)
            {
                context.sendTranslated("&cPortal &6%s&c not found!", context.getString("p"));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated("&cYou need to define a portal to use!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        if (context.getString(0).equalsIgnoreCase("here"))
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated("&eThe Portal Agency will bring you your portal for just &6$ 1337&e within &6%d weeks",
                                       new Random().nextInt(51)+1);
                return;
            }
            portal.config.destination = new Destination(((User)context.getSender()).getLocation());
        }
        else if (context.getString(0).startsWith("p:"))
        {
            Portal destPortal = manager.getPortal(context.getString(0).substring(2));
            if (destPortal == null)
            {
                context.sendTranslated("&cPortal &6%s&c not found!", context.getString(0).substring(2));
                return;
            }
            portal.config.destination = new Destination(destPortal);
        }
        else
        {
            World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
            if (world == null)
            {
                context.sendTranslated("&cWorld &6%s&c not found!", context.getString(0));
                return;
            }
            portal.config.destination = new Destination(world);
        }
        portal.config.save();
        context.sendTranslated("&aPortal destination set!");
    }

    @Command(desc = "Changes a portals location", usage = "[portal]", max = 1)
    public void location(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Selector selector = this.getModule().getCore().getModuleManager().getServiceManager().getServiceImplementation(Selector.class);
            if (selector.getSelection(sender) instanceof Cuboid)
            {
                Portal portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
                if (portal == null)
                {
                    context.sendTranslated("&cYou need to define a portal!");
                    context.sendMessage(context.getCommand().getUsage(context));
                    return;
                }
                Location p1 = selector.getFirstPoint(sender);
                Location p2 = selector.getSecondPoint(sender);
                portal.config.location.from = new BlockVector3(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ());
                portal.config.location.to = new BlockVector3(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
                portal.config.save();
                context.sendTranslated("&aPortal &6%s&a updated to your current selection!", portal.getName());
                return;
            }
            context.sendTranslated("&cPlease select a cuboid first!");
            return;
        }
        context.sendTranslated("&cYou have to be ingame to do this!");
    }

    @Command(desc = "Modifies the location where a player exits when teleporting a portal", usage = "[portal]", max = 1)
    public void exit(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            Portal portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
            if (portal == null)
            {
                context.sendTranslated("&cYou need to define a portal!");
                context.sendMessage(context.getCommand().getUsage(context));
                return;
            }
            Location location = ((User)context.getSender()).getLocation();
            if (portal.config.world.getWorld() != location.getWorld())
            {
                context.sendTranslated("&cA portals exit cannot be in an other world than its location!");
                return;
            }
            portal.config.location.destination = new WorldLocation(location);
            portal.config.save();
            return;
        }
        context.sendTranslated("&cYou have to be ingame to do this!");
    }

    @Command(desc = "Toggles safe teleportation for this portal", usage = "[portal]", max = 1)
    public void togglesafe(CommandContext context)
    {
        Portal portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        if (portal == null)
        {
            context.sendTranslated("&cYou need to define a portal!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        portal.config.safeTeleport = !portal.config.safeTeleport;
        portal.config.save();
        if (portal.config.safeTeleport)
        {
            context.sendTranslated("&aThe portal &6%s&a will not teleport to an unsafe destination");
        }
        else
        {
            context.sendTranslated("&aThe portal &6%s&a will also teleport to an unsafe destination");
        }
    }

    @Command(desc = "Toggles whether entities can teleport with this portal", usage = "[portal]", max = 1)
    public void entity(CommandContext context)
    {
        Portal portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        if (portal == null)
        {
            context.sendTranslated("&cYou need to define a portal!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        portal.config.teleportNonPlayers = !portal.config.teleportNonPlayers;
        portal.config.save();
        if (portal.config.teleportNonPlayers)
        {
            context.sendTranslated("&aThe portal &6%s&a will teleport entities too", portal.getName());
        }
        else
        {
            context.sendTranslated("&aThe portal &6%s&a will only teleport players", portal.getName());
        }
    }
}
