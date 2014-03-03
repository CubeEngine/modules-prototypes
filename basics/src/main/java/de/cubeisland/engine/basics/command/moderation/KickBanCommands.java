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
package de.cubeisland.engine.basics.command.moderation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.TimeConversionException;
import de.cubeisland.engine.core.util.formatter.MessageType;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

/**
 * Contains commands to manage kicks/bans.
 * /kick
 * /ban
 * /unban
 * /ipban
 * /ipunban
 */
public class KickBanCommands
{
    private final Basics module;
    private final BanManager banManager;
    private final UserManager um;

    private static final String kickMessage = "&cYou got kicked from the server!\n&r";
    private static final String banMessage = "&cYou got banned from this server!\n&r";

    public KickBanCommands(Basics module)
    {
        this.module = module;
        this.banManager = this.module.getCore().getBanManager();
        this.um = this.module.getCore().getUserManager();
    }

    @Command(desc = "Kicks a player from the server",
             usage = "<*|<player>> [reason]", min = 1, max = NO_MAX)
    public void kick(ParameterizedContext context)
    {
        String reason;
        reason = this.getReasonFrom(context, 1, module.perms().COMMAND_KICK_NOREASON);
        if (reason == null) return;
        if (context.getString(0).equals("*"))
        {
            if (!module.perms().COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to kick everyone!");
                return;
            }
            String sendername = context.getSender().getName();
            for (User toKick : this.um.getOnlineUsers())
            {
                if (!sendername.equalsIgnoreCase(toKick.getName()))
                {
                    toKick.kickPlayer(toKick.composeMessage(MessageType.NONE, kickMessage) + "\n" +  reason);
                }
            }
            return;
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "User &2%s&c not found!", context.getString(0));
            return;
        }
        user.kickPlayer(user.composeMessage(MessageType.NONE, kickMessage) + "\n" +  reason);
        this.um.broadcastMessageWithPerm("&2%s&c was kicked from the server by &2%s&c!\n%s", module.perms().KICK_RECEIVEMESSAGE,
                         user.getName(), context.getSender().getName(), reason);
    }

    @Command(names = {"ban", "kickban"},
             desc = "Bans a player permanently on your server.",
             min = 1, max = NO_MAX,
             usage = "<player> [reason] [-ipban]",
             flags = {@Flag(longName = "ipban", name = "ip"),
             @Flag(longName = "force", name = "f")})
    public void ban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context)) return;
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        User user = null;
        if (player.hasPlayedBefore() || player.isOnline())
        {
            user = um.getExactUser(player.getName());
        }
        else if (!context.hasFlag("f"))
        {
            context.sendTranslated(MessageType.NEUTRAL,"&2%s&6 has never played on this server before! Use the -force flag to ban him anyways.", player.getName());
            return;
        }
        String reason = this.getReasonFrom(context, 1, module.perms().COMMAND_BAN_NOREASON);
        if (reason == null) return;
        if (context.hasFlag("ip"))
        {
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "You cannot ipBan a player that has never player on the server before!");
                return;
            }
            if (user.getAddress() != null)
            {
                InetAddress ipAdress = user.getAddress().getAddress();
                if (this.banManager.isIpBanned(ipAdress))
                {
                    context.sendTranslated(MessageType.NEGATIVE, "&2%s&c is already ip-banned!", player.getName());
                    return;
                }
                this.banManager.addBan(new IpBan(ipAdress,context.getSender().getName(), reason));
                Set<String> bannedUsers = new HashSet<>();
                for (User ipPlayer : um.getOnlineUsers())
                {
                    if (ipPlayer.getAddress() != null && ipPlayer.getAddress().getAddress().equals(ipAdress))
                    {
                        ipPlayer.kickPlayer(ipPlayer.composeMessage(MessageType.NONE, banMessage) + "\n" + reason);
                        bannedUsers.add(ipPlayer.getName());
                    }
                }
                context.sendTranslated(MessageType.NEGATIVE, "You banned the IP: &e%s&c!", ipAdress.getHostAddress());
                um.broadcastMessageWithPerm("&2%s&c was banned from the server by &2%s&c!", module.perms().BAN_RECEIVEMESSAGE,
                            user.getName(), context.getSender().getName());
                um.broadcastMessageWithPerm(reason, module.perms().BAN_RECEIVEMESSAGE);
                um.broadcastMessageWithPerm("&cAnd with it kicked: &2%s&c!", module.perms().BAN_RECEIVEMESSAGE,
                            StringUtils.implode(ChatFormat.parseFormats("&c,&2"), bannedUsers));
            }
            else
            {
                context.sendTranslated(MessageType.NEUTRAL, "You cannot IP-ban this player because he was offline for too long!");
            }
            return;
        }
        else
        {
            if (this.banManager.isUserBanned(player.getName()))
            {
                context.sendTranslated(MessageType.NEGATIVE, "&2%s&c is already banned!", player.getName());
                return;
            }
            this.banManager.addBan(new UserBan(player.getName(),context.getSender().getName(), reason));
            if (user != null)
            {
                user.kickPlayer(user.composeMessage(MessageType.NONE, banMessage) + "\n" +  reason);
            }
        }
        context.sendTranslated(MessageType.NEGATIVE, "You banned &2%s&c!", player.getName());
        um.broadcastMessageWithPerm("&2%s&c was banned from the server by &2%s&c!",
                module.perms().BAN_RECEIVEMESSAGE , player.getName(), context.getSender().getName());
        um.broadcastMessageWithPerm(reason, module.perms().BAN_RECEIVEMESSAGE);
    }

    private String getReasonFrom(CommandContext context, int at, Permission permNeeded)
    {
        String reason = "";
        if (context.hasArg(at))
        {
            reason = ChatFormat.parseFormats(context.getStrings(at));
        }
        else if (!permNeeded.isAuthorized(context.getSender()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You need to specify a reason!");
            return null;
        }
        return reason;
    }

    @Command(names = {"unban", "pardon"},
             desc = "Unbans a previously banned player.",
             min = 1, max = 1, usage = "<player>")
    public void unban(CommandContext context)
    {
        String userName = context.getString(0);
        if (!this.banManager.isUserBanned(userName))
        {
            context.sendTranslated(MessageType.NEGATIVE, "&2%s&c is not banned, maybe you misspelled his name?", userName);
            return;
        }
        this.banManager.removeUserBan(userName);
        context.sendTranslated(MessageType.POSITIVE, "You unbanned &2%s&a!", userName);
    }

    @Command(names = {"ipban", "banip"},
             desc = "Bans the IP from this server.",
             min = 1, max = 2, usage = "<IP address> [reason]")
    public void ipban(CommandContext context)
    {
        String ipaddress = context.getString(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipaddress);
            if (this.banManager.isIpBanned(address))
            {
                context.sendTranslated(MessageType.NEUTRAL, "The IP &6%s&e is already banned!", address.getHostAddress());
                return;
            }
            String reason = this.getReasonFrom(context,1, module.perms().COMMAND_IPBAN_NOREASON);
            if (reason == null) return;
            this.banManager.addBan(new IpBan(address,context.getSender().getName(), reason));
            context.sendTranslated(MessageType.NEGATIVE, "You banned the IP &6%s &cfrom your server!", address.getHostAddress());
            Set<String> bannedUsers = new HashSet<>();
            for (User user : um.getOnlineUsers())
            {
                if (user.getAddress() != null && user.getAddress().getAddress().getHostAddress().equals(ipaddress))
                {
                    user.kickPlayer(user.composeMessage(MessageType.NONE, banMessage) + "\n" +  reason);
                    bannedUsers.add(user.getName());
                }
            }
            um.broadcastMessageWithPerm("&cThe IP &6%s&c was banned from the server by &2%s&c!",
                    module.perms().BAN_RECEIVEMESSAGE, ipaddress, context.getSender().getName());
            um.broadcastMessageWithPerm(reason, module.perms().BAN_RECEIVEMESSAGE);
            if (!bannedUsers.isEmpty())
            {
                um.broadcastMessageWithPerm("&cAnd with it kicked: &2%s!",
                        module.perms().BAN_RECEIVEMESSAGE, StringUtils.implode(ChatFormat.parseFormats("&c,&2"), bannedUsers));
            }
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated(MessageType.NEGATIVE, "&6%s&c is not a valid IP-address!", ipaddress);
        }
    }

    @Command(names = {"ipunban", "unbanip", "pardonip"},
             desc = "Bans the IP from this server.",
             min = 1, max = 1, usage = "<IP address>")
    public void ipunban(CommandContext context)
    {
        String ipadress = context.getString(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipadress);
            this.banManager.removeIpBan(address);
            context.sendTranslated(MessageType.POSITIVE, "You unbanned the IP &6%s&a!", address.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated(MessageType.NEGATIVE, "&6%s&c is not a valid IP-address!", ipadress);
        }
    }

    @Command(names = {"tempban","tban"},
             desc = "Bans a player for a given time.",
             min = 2, max = NO_MAX,
             usage = "<player> <time> [reason]",
             flags = @Flag(longName = "force", name = "f"))
    public void tempban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context)) return;
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        User user = null;
        if (player.hasPlayedBefore() || player.isOnline())
        {
            user = um.getExactUser(player.getName());
        }
        else if (!context.hasFlag("f"))
        {
            context.sendTranslated(MessageType.NEUTRAL, "&2%s&6 has never played on this server before! Use the -force flag to ban him anyways.", player.getName());
            return;
        }
        String reason = this.getReasonFrom(context, 2, module.perms().COMMAND_TEMPBAN_NOREASON);
        if (reason == null) return;
        if (this.banManager.isUserBanned(player.getName()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "&2%s&c is already banned!", player.getName());
            return;
        }
        try
        {
            long millis = StringUtils.convertTimeToMillis(context.getString(1));
            Date toDate = new Date(System.currentTimeMillis() + millis);
            this.banManager.addBan(new UserBan(player.getName(),context.getSender().getName(), reason, toDate));
            if (player.isOnline())
            {
                if (user == null) throw new IllegalStateException();
                user.kickPlayer(user.composeMessage(MessageType.NONE, banMessage) + "\n" +  reason);
            }
            context.sendTranslated(MessageType.POSITIVE, "You banned &2%s&a temporarily!", player.getName());
            um.broadcastMessageWithPerm("&2%s &cwas banned temporarily from the server by &2%s&c!\n%s",
                        module.perms().BAN_RECEIVEMESSAGE, player.getName(), context.getSender().getName(), reason);
        }
        catch (TimeConversionException ex)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Invalid time value! &eExamples: 1d 12h 5m");
        }
    }

    private boolean cannotBanUser(CommandContext context)
    {
        if (!Bukkit.getOnlineMode())
        {
            if (this.module.getConfiguration().commands.disallowBanIfOfflineMode)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Banning players by name is not allowed in offline-mode!\n"
                                     + "&eYou can change this in your Basics-Configuration.");
                return true;
            }
            context.sendTranslated(MessageType.NEUTRAL, "The server is running in &4OFFLINE-mode&e.\n"
                                 + "Players could change their username with a cracked client!\n"
                                 + "&aYou can IP-ban to prevent banning a real player in that case.");
        }
        return false;
    }

    @Command(desc = "Reloads the ban lists")
    public void reloadbans(CommandContext context)
    {
        this.banManager.reloadBans();
        context.sendTranslated(MessageType.POSITIVE, "Reloaded the ban lists successfully!");
    }
}
