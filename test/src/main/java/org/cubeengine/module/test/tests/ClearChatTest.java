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
package org.cubeengine.module.test.tests;

import org.cubeengine.butler.parametric.Command;
import org.cubeengine.service.command.CommandContext;
import org.cubeengine.service.user.User;
import org.cubeengine.module.core.util.ChatFormat;

public class ClearChatTest extends Test
{
    private final org.cubeengine.module.test.Test module;
    private static final int MAX_CHAT_LINES = 100;

    public ClearChatTest(org.cubeengine.module.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        module.getCore().getCommandManager().addCommands(module.getCore().getCommandManager(), module, this);
        this.setSuccess(true);
    }

    @Command(alias = "cls", desc = "Clears the chat")
    public void clearscreen(CommandContext context)
    {
        if (context.getSource() instanceof User)
        {
            for (int i = 0; i < MAX_CHAT_LINES; ++i)
            {
                context.sendMessage(" ");
            }
        }
        else
        {
            context.sendMessage(ChatFormat.RED + "You better don't do this.");
        }
    }
}
