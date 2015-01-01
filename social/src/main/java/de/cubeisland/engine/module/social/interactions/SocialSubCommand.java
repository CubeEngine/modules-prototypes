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
package de.cubeisland.engine.module.social.interactions;

import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.methodic.Command;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import de.cubeisland.engine.module.social.Social;

import com.restfb.exception.FacebookException;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;

import static de.cubeisland.engine.core.util.formatter.MessageType.CRITICAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class SocialSubCommand
{
    private final Social module;

    public SocialSubCommand(Social module)
    {
        this.module = module;
    }

    @Command(desc = "post a message")
    public void post(CommandContext context)
    {
        CommandSource sender = context.getSource();
        if (sender instanceof User)
        {
            User user = (User)sender;
            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.getPositionalCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            try
            {
                context.sendTranslated(POSITIVE, "Your message has been posted, id: {}", module.getFacebookManager().getUser(user)
                                                                                     .publishMessage(message.toString())
                                                                                     .getId());
            }
            catch (FacebookException ex)
            {
                context.sendTranslated(NEGATIVE, "Your message could for some reason not be sent.");
                context.sendTranslated(NEGATIVE, "The error message: {}", ex.getLocalizedMessage());
            }
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You have to be a player to use this command");
        }
    }

    @Command(desc = "sign like!")
    public void sign(CommandContext context)
    {
        if (!context.isSource(User.class))
        {
            context.sendTranslated(CRITICAL, "You can't execute this command from the console");
            return;
        }

        User sender = (User)context.getSource();

        Block targetBlock = sender.getTargetBlock(null, 9);
        if (targetBlock == null)
        {
            context.sendTranslated(NEGATIVE, "You have to look at a sign less than 9 meters away.");
            return;
        }
        if (!(targetBlock.getType() == Material.SIGN || targetBlock.getType() == Material.WALL_SIGN || targetBlock.getType() == Material.SIGN_POST))
        {
            context.sendTranslated(NEGATIVE, "You have to look at a sign less than 9 meters away");
            return;
        }

        Sign targetSign = (Sign)targetBlock.getState();
        try
        {
            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.getPositionalCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            String id = module.getFacebookManager().getUser(sender).publishMessage(message.toString()).getId();
            module.getFacebookManager().addPosts(targetBlock.getLocation(), id);
            targetSign.setLine(0, ChatFormat.parseFormats("&b" + ChatFormat.stripFormats(targetSign.getLine(0))));
        }
        catch (Exception ex)
        {
            targetSign.setLine(0, ChatFormat.parseFormats("&c" + ChatFormat.stripFormats(targetSign.getLine(0))));
            context.sendTranslated(NEGATIVE, "An error occurred while posting the message =(");
            context.sendTranslated(NEGATIVE, "The error message: {}", ex.getLocalizedMessage());
        }
        finally
        {
            targetSign.update();
        }
    }
}
