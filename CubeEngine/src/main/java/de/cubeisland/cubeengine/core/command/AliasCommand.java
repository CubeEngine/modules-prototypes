package de.cubeisland.cubeengine.core.command;

import java.util.List;

/**
 *
 * @author Phillip Schichtel
 */
public class AliasCommand extends CubeCommand
{
    private final CubeCommand command;

    public AliasCommand(String name, List<String> aliases, CubeCommand command)
    {
        super(command.getModule(), name, command.getDescription(), command.getUsage(), aliases);
        this.command = command;
    }

    @Override
    public void run(CommandContext context)
    {
        this.command.run(context);
    }

    @Override
    public void addChild(CubeCommand command)
    {
        this.command.addChild(command);
    }

    @Override
    public void removeChild(String command)
    {
        this.command.removeChild(command);
    }

    @Override
    public boolean hasChild(String name)
    {
        return this.command.hasChild(name);
    }
}