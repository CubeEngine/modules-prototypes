package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 *
 * @author Anselm Brehme
 */
public abstract class LogSubConfiguration extends Configuration
{
    @Option(value = "enabled")
    public boolean enabled;

    public LogSubConfiguration()
    {
        this.setCodec(Configuration.resolveCodec("yml"));
    }

    public abstract String getName();
}
