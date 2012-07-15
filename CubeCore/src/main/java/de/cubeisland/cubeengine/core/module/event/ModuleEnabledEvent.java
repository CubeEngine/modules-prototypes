package de.cubeisland.cubeengine.core.module.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleEnabledEvent extends ModuleEvent
{
    public ModuleEnabledEvent(Core core, Module module)
    {
        super(core, module);
    }


    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
