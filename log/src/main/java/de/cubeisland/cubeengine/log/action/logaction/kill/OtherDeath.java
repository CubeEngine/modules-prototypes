package de.cubeisland.cubeengine.log.action.logaction.kill;


import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.KILL;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * other-death
 * <p>Events: {@link KillActionType}</p>
 */
public class OtherDeath extends SimpleLogActionType

{
    public OtherDeath(Log module)
    {
        super(module, PLAYER, ENTITY, KILL);
    }

    @Override
    public String getName()
    {
        return "other-death";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        KillActionType.showSubActionLogEntry(user, logEntry,time,loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).OTHER_DEATH_enable;
    }
}
