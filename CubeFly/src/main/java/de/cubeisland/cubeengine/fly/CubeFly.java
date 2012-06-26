package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.command.BaseCommand;
import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import java.io.File;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class CubeFly extends ModuleBase
{
    public static boolean debugMode = false;
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.fly";
    private BaseCommand baseCommand;
    private FlyConfiguration config;

    public CubeFly()
    {
        super("fly");
    }

    @Override
    public void onEnable()
    {
        CubeEngine.registerModule(this);
        
        this.pm = this.getServer().getPluginManager();

        this.config = Configuration.load(this, FlyConfiguration.class);

        this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        this.baseCommand.registerCommands(new FlyCommand()).setDefaultCommand("fly").unregisterCommand("reload");
        this.getCommand("fly").setExecutor(baseCommand);

        this.pm.registerEvents(new FlyListener(CubeCore.getInstance().getUserManager(), this), this);

        CubeEngine.registerPermissions(Perm.values());
    }
}
