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

import de.cubeisland.engine.butler.CommandInvocation;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.result.CommandResult;
import org.cubeengine.service.command.CommandContext;
import org.cubeengine.service.command.CommandSender;
import de.cubeisland.engine.service.command.result.AsyncResult;

public class AsyncCommandTest extends Test
{
    private final org.cubeengine.module.test.Test module;

    public AsyncCommandTest(org.cubeengine.module.test.Test module)
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
