package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import gnu.trove.map.hash.THashMap;

import java.util.HashMap;
import java.util.Map;

@Codec("yml")
public class LogConfiguration extends Configuration
{
    @Option("enable-logging")
    public boolean enableLogging = true;

    @Option("log-actions")
    public Map<String, LogActionConfig> logActionConfigs = new THashMap<String, LogActionConfig>();

    public Map<Class<? extends SubLogConfig>,SubLogConfig> subConfigs = new HashMap<Class<? extends SubLogConfig>, SubLogConfig>();

    public LogConfiguration()
    {
        this.onLoaded(); // This has to be here to initialize all the logActionConfigs for loading
    }

    @Override
    public void onLoaded()
    {
        for (LogAction action : LogAction.values())
        {
            LogActionConfig laConfig = this.getActionConfig(action);
            if (laConfig == null)
            {
                laConfig = new LogActionConfig();
                laConfig.enabled = action.isDefaultEnabled();
                for (SubLogConfig subLogConfig: action.getConfigs())
                {
                    laConfig.subLogConfigs.put(subLogConfig.getName(),subLogConfig);
                    this.subConfigs.put(subLogConfig.getClass(),subLogConfig);
                }
                this.logActionConfigs.put(action.name(), laConfig);
            }
            else
            {
                for (SubLogConfig subLogConfig: laConfig.subLogConfigs.values())
                {
                    this.subConfigs.put(subLogConfig.getClass(),subLogConfig);
                }
            }
        }
    }

    public LogActionConfig getActionConfig(LogAction action)
    {
        return this.logActionConfigs.get(action.name());
    }

    public <T extends SubLogConfig> T getSubLogConfig(Class<T> configClass)
    {
        return (T)this.subConfigs.get(configClass);
    }
}
