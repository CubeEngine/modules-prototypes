package de.cubeisland.cubeengine.core.module.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEvent;
import de.cubeisland.cubeengine.core.module.Module;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class ModuleEvent extends CubeEvent
{
    private final Module module;

    public ModuleEvent(Core core, Module module)
    {
        super(core);
        this.module = module;
    }

    public Module getModule()
    {
        return this.module;
    }
}