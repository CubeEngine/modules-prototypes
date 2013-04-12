package de.cubeisland.cubeengine.travel.interactions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.Home;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.TeleportPoint;

public class HomeAdminCommand extends ContainerCommand
{
    private static final Long ACCEPT_TIMEOUT = 20000l;

    private final Map<String, Pair<Long, ParameterizedContext>> acceptEntries;
    private final TelePointManager tpManager;
    private final Travel module;

    public HomeAdminCommand(Travel module)
    {
        super(module, "admin", "Teleport to another users home");
        this.module = module;
        this.tpManager = module.getTelepointManager();
        this.acceptEntries = new HashMap<String, Pair<Long, ParameterizedContext>>();

        this.setUsage("[User] [Home]");
        this.getContextFactory().setArgBounds(new ArgBounds(0, 2));
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.isSender(User.class))
        {
            User sender = (User)context.getSender(); //TODO console
            User user = context.getUser(0);
            Home home;
            if (user == null)
            {
                sender.sendTranslated("%s is not an user on this server!", context.getString(0));
                return null;
            }

            if (context.getArgCount() == 2)
            {
                home = tpManager.getHome(user, context.getString(1));
                if (home == null)
                {
                    sender.sendTranslated("%s does not have a home named %s", user.getName(), context.getString(1));
                    return null;
                }
            }
            else
            {
                home = tpManager.getHome(user, "home");
                if (home == null)
                {
                    sender.sendTranslated("%s does not have a home ", user.getName());
                    return null;
                }
            }

            sender.teleport(home.getLocation());
            if (home.getWelcomeMsg() != null)
            {
                sender.sendMessage(home.getWelcomeMsg());
            }
            else
            {
                sender.sendTranslated("You have been teleported to %s's home", user.getName());
            }
            return null;
        }
        else
        {
            return super.run(context);
        }
    }

    @Alias(names = {
        "clearhomes"
    })
    @Command(desc = "Clear all homes (of an user)", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "Private")
    }, permDefault =  PermDefault.OP, max = 1, usage = " <user> <-public> <-Private>")
    public void clear(ParameterizedContext context)
    {
        if (context.getArgCount() > 0)
        {
            if (CubeEngine.getUserManager().getUser(context.getString(0), false) == null)
            {
                context.sendTranslated("&3%s &4Isn't an user on this server", context.getString(0));
                return;
            }
            else
            {
                if (context.hasFlag("pub"))
                {
                    context
                        .sendTranslated("&5Are you sure you want to delete all public homes ever created by &3%s", context
                            .getString(0));
                    context
                        .sendTranslated("&5To delete all the public homes, do: &9\"/home admin accept\" &5before 20 secunds");
                }
                else if (context.hasFlag("priv"))
                {
                    context
                        .sendTranslated("&5Are you sure you want to delete all private homes ever created by &3%s", context
                            .getString(0));
                    context
                        .sendTranslated("&5To delete all the private homes, do: &9\"/home admin accept\" &5before 20 secunds");
                }
                else
                {
                    context.sendTranslated("&5Are you sure you want to delete all homes ever created by &3%s", context
                        .getString(0));
                    context
                        .sendTranslated("&5To delete all the homes, do: &9\"/home admin accept\" &5before 20 secunds");
                }
            }
        }
        else
        {
            if (context.hasFlag("pub"))
            {
                context
                    .sendTranslated("&5Are you sure you want to delete all public homes ever created on this server!?");
                context
                    .sendTranslated("&5To delete all the public homes of every user, do: &9\"/home admin accept\" &5before 20 secunds");
            }
            else if (context.hasFlag("priv"))
            {
                context
                    .sendTranslated("&5Are you sure you want to delete all private homes ever created on this server!?");
                context
                    .sendTranslated("&5To delete all the private homes of every user, do: &9\"/home admin accept\" &5before 20 secunds");
            }
            else
            {
                context.sendTranslated("&5Are you sure you want to delete all homes ever created on this server!?");
                context
                    .sendTranslated("&5To delete all the homes of every user, do: &9\"/home admin accept\" &5before 20 secunds");
            }
        }
        acceptEntries.put(context.getSender().getName(), new Pair<Long, ParameterizedContext>(System
                                                                                                  .currentTimeMillis(), context));
    }

    @Alias(names = {"accept"})
    @Command(desc = "accept your previous interactions", permDefault =  PermDefault.OP,  min = 0, max = 0)
    public void accept(ParameterizedContext context)
    {
        if (this.acceptEntries.containsKey(context.getSender().getName()))
        {
            if (this.acceptEntries.get(context.getSender().getName()).getLeft() + ACCEPT_TIMEOUT > System
                .currentTimeMillis())
            {
                ParameterizedContext usedContext = this.acceptEntries.get(context.getSender().getName()).getRight();
                if (usedContext.getCommand().getName().equals("clear"))
                {
                    if (usedContext.getArgCount() == 0)
                    { // No user
                        int mask = usedContext.getFlagCount() == 0 ? tpManager.ALL : 0;
                        if (context.hasFlag("pub"))
                        {
                            mask |= tpManager.PUBLIC;
                        }
                        if (context.hasFlag("priv"))
                        {
                            mask |= tpManager.PRIVATE;
                        }
                        tpManager.deleteHomes(mask);
                        context.sendTranslated("The homes are now deleted");
                    }
                    else
                    {
                        User user = usedContext.getUser(0);
                        int mask = usedContext.getFlagCount() == 0 ? tpManager.ALL : 0;
                        if (context.hasFlag("pub"))
                        {
                            mask |= tpManager.PUBLIC;
                        }
                        if (context.hasFlag("priv"))
                        {
                            mask |= tpManager.PRIVATE;
                        }
                        tpManager.deleteHomes(user, mask);
                        context.sendTranslated("The homes are now deleted");
                    }
                }
                return;
            }
        }
        context.sendTranslated("&4You have nothing to accept");
    }

    @Command(desc = "List all (public) homes", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private"),
        @Flag(name = "o", longName = "owned"),
        @Flag(name = "i", longName = "invited")
    }, permDefault =  PermDefault.OP, min = 0, max = 1, usage = " <<user>  <-owned> <-invited>> <-public> <-private>")
    public void list(ParameterizedContext context)
    {
        int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
        if (context.hasFlag("pub"))
        {
            mask |= tpManager.PUBLIC;
        }
        if (context.hasFlag("priv"))
        {
            mask |= tpManager.PRIVATE;
        }
        if (context.hasFlag("o"))
        {
            mask |= tpManager.OWNED;
        }
        if (context.hasFlag("i"))
        {
            mask |= tpManager.INVITED;
        }

        Set<Home> homes;
        if (context.getArgCount() == 0)
        {
            homes = tpManager.listHomes(mask);
        }
        else
        {
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("Can't find any user named %s", context.getString(0));
                return;
            }
            homes = tpManager.listHomes(user, mask);
        }
        if (homes.isEmpty())
        {
            context.sendTranslated("The query returned no homes!");
            return;
        }
        for (Home home : homes)
        {
            if (home.isPublic())
            {
                context.sendTranslated("  &2public&e:&6%s", home.getName());
            }
            else
            {
                context.sendTranslated("  &2%s&e:&6%s", home.getOwner().getName(), home.getName());
            }
        }
    }

    @Command(names = {
        "private"
    }, permDefault =  PermDefault.OP, desc = "Make a users home private", min = 1, max = 1, usage = " owner:home")
    public void makePrivate(CommandContext context)
    {
        User user = CubeEngine.getUserManager().getUser(context.getString(0), false);
        Home home = null;
        home = tpManager.getHome(context.getString(0));
        if (home == null)
        {
            context.sendTranslated("&4Couldn't find &9%s", context.getString(0));
            return;
        }
        if (!home.isPublic())
        {
            context.sendTranslated("&9%s &6is already private!", context.getString(0));
            return;
        }
        home.setVisibility(TeleportPoint.Visibility.PRIVATE);
        context.sendTranslated("&9%s &6is now private", context.getString(0));
    }

    @Command(names = {
        "public"
    }, permDefault =  PermDefault.OP, desc = "Make a users home public", min = 1, max = 1, usage = " owner:home")
    public void makePublic(CommandContext context)
    {
        User user = CubeEngine.getUserManager().getUser(context.getString(0), false);
        Home home = null;
        home = tpManager.getHome(context.getString(0));
        if (home == null)
        {
            context.sendTranslated("&4Couldn't find &9%s", context.getString(0));
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated("&9%s &6is already public!", context.getString(0));
            return;
        }
        home.setVisibility(TeleportPoint.Visibility.PUBLIC);
        context.sendTranslated("&9%s &6is now public", context.getString(0));
    }

    private class AcceptEntry
    {
        private CommandContext context;

        public AcceptEntry(CommandContext context)
        {
            this.context = context;
        }
    }
}