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

import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.module.social.Social;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class SocialCommand
{
    private final Social module;

    public SocialCommand(Social module)
    {
        this.module = module;
    }

    @Command(name = "facebook", alias = "fb", desc = "Facebook")
    @Params(nonpositional = {
        @Param(names = {"user", "u"}, type = User.class), @Param(names = {"code", "c"}, type = String.class)
    })
    public void facebook(CommandContext context)
    {
        if (!context.isSource(User.class) && !context.hasNamed("user"))
        {
            context.sendTranslated(NEGATIVE, "You have to include a player to log in");
            return;
        }

        User user;
        if (context.hasNamed("user"))
        {
            user = context.get("user");
        }
        else
        {
            user = (User)context.getSource();
        }

        if (context.hasNamed("code"))
        {
            String verifyCode = context.getString("code");
            module.getFacebookManager().initializeUser(user, verifyCode);
            return;
        }

        context.sendTranslated(POSITIVE, "Here is your auth address: {}", module.getFacebookManager().getAuthURL(user));

        // @Quick_Wango This is where you need to get the response
    }
}
