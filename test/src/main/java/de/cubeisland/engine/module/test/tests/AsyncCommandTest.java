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
package de.cubeisland.engine.module.test.tests;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.result.CommandResult;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.result.AsyncResult;

public class AsyncCommandTest extends Test
{
    private final de.cubeisland.engine.module.test.Test module;

    public AsyncCommandTest(de.cubeisland.engine.module.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        module.getCore().getCommandManager().addCommands(module.getCore().getCommandManager(), module, this);
        this.setSuccess(true);
    }

    @Command(desc = "A command that tests async execution.")
    public CommandResult asyncCommand(CommandContext context)
    {
        context.sendMessage("Async GO!");
        return new AsyncResult(module) {
            @Override
            public void main(CommandInvocation sender)
            {
                try
                {
                    Thread.sleep(1000 * 5L);
                }
                catch (InterruptedException ignored)
                {}
                ((CommandSender)sender.getCommandSource()).sendMessage("Delayed!");
                try
                {
                    Thread.sleep(1000 * 5L);
                }
                catch (InterruptedException ignored)
                {}
            }

            @Override
            public void onFinish(CommandInvocation context)
            {
                ((CommandSender)context.getCommandSource()).sendMessage("Finished!");
            }
        };
    }
}
