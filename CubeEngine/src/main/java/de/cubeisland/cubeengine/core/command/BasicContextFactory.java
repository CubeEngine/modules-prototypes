package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class BasicContextFactory implements ContextFactory
{
    private ArgBounds bounds;

    public BasicContextFactory(ArgBounds bounds)
    {
        this.bounds = bounds;
    }

    @Override
    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    public void setArgBounds(ArgBounds newBounds)
    {
        this.bounds = newBounds;
    }

    @Override
    public BasicContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        if (commandLine.length < this.getArgBounds().getMin())
        {
            throw new IncorrectUsageException("You've given too few arguments.");
        }
        if (this.getArgBounds().getMax() > ArgBounds.NO_MAX && commandLine.length > this.getArgBounds().getMax())
        {
            throw new IncorrectUsageException("You've given too many arguments.");
        }
        return new BasicContext(command, sender, labels, new LinkedList<String>(Arrays.asList(commandLine)));
    }

    @Override
    public CommandContext parse(CubeCommand command, CommandContext context)
    {
        return new BasicContext(command, context.getSender(), context.getLabels(), context.getArgs());
    }
}
