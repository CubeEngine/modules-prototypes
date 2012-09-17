package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseFactory;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("This is the bukkit plugin")
public class BukkitCore extends JavaPlugin implements Core
{
    private Database database;
    private PermissionRegistration permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;
    private ModuleManager moduleManager;
    private I18n i18n;
    private CoreConfiguration config;
    private CubeLogger logger;
    private EventManager eventRegistration;
    private Server server;
    private CommandManager commandManager;
    private ScheduledExecutorService executor;
    private TableManager tableManager;

    @Override
    public void onEnable()
    {
        CubeEngine.initialize(this);
        
        this.server = this.getServer();
        PluginManager pm = this.server.getPluginManager();

        this.logger = new CubeLogger("Core");
        // TODO RemoteHandler is not yet implemented this.logger.addHandler(new RemoteHandler(Level.SEVERE, this));

        try
        {
            this.fileManager = new FileManager(this.getDataFolder());
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, "Failed to initialize the FileManager", e);
            pm.disablePlugin(this);
            return;
        }
        
        this.fileManager.dropResources(CoreResource.values());
        
        try
        {
            // depends on: file manager
            this.logger.addHandler(new FileHandler(Level.ALL, new File(this.fileManager.getLogDir(), "core").toString()));
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        
        // depends on: file manager
        this.config = Configuration.load(CoreConfiguration.class, new File(fileManager.getDataFolder(), "core.yml"));
        
        // depends on: core config and file manager
        this.database = DatabaseFactory.loadDatabase(this.config.database, new File(fileManager.getDataFolder(), "database.yml"));
        if (this.database == null)
        {
            this.logger.log(Level.SEVERE, "Could not connect to the database type ''{0}''", this.config.database);
            pm.disablePlugin(this);
            return;
        }
		// depends on: database
        this.tableManager = new TableManager(this);
        
        // depends on: plugin manager
        this.permissionRegistration = new PermissionRegistration(pm);

        // depends on: permission registration
        this.registerPermissions(Perm.values());
        
        // depends on: plugin manager
        this.eventRegistration = new EventManager(pm);
        
        // depends on: core config
        this.executor = Executors.newScheduledThreadPool(this.config.executorThreads);
        
        // depends on: executor, database, Server, core config and event registration
        this.userManager = new UserManager(this);
        
        // depends on: file manager, core config
        this.i18n = new I18n(this.fileManager, this.config.defaultLanguage);
        
        // depends on: Server
        this.commandManager = new CommandManager(this);
        
        // depends on: database
        this.moduleManager = new ModuleManager(this);

        // depends on: file manager
        this.moduleManager.loadModules(this.fileManager.getModulesDir());
        
        // depends on: finshed loading modules
        this.getUserManager().cleanDB();
        
        // depends on: server
        BukkitUtils.registerPacketHookInjector(this, pm);
    }

    @Override
    public void onDisable()
    {
        CubeEngine.clean();

        if (this.moduleManager != null)
        {
            this.moduleManager.clean();
            this.moduleManager = null;
        }

        this.fileManager = null;

        if (this.userManager != null)
        {
            this.userManager.clean();
            this.userManager = null;
        }

        this.permissionRegistration = null;

        if (this.i18n != null)
        {
            this.i18n.clean();
            this.i18n = null;
        }
        if (this.executor != null)
        {
            try
            {
                this.executor.shutdown();
                this.executor.awaitTermination(config.executorTermination, TimeUnit.SECONDS);
            }
            catch (InterruptedException ex)
            {
                this.logger.log(Level.SEVERE, "Could not execute all pending tasks", ex);
            }
            finally
            {
                this.executor = null;
            }
        }
    }

    @Override
    public Database getDB()
    {
        return this.database;
    }

    @Override
    public PermissionRegistration getPermissionRegistration()
    {
        return this.permissionRegistration;
    }

    @Override
    public UserManager getUserManager()
    {
        return userManager;
    }

    @Override
    public FileManager getFileManager()
    {
        return this.fileManager;
    }

    public void registerPermissions(Permission... values)
    {
        this.permissionRegistration.registerPermissions(values);
    }

    @Override
    public ModuleManager getModuleManager()
    {
        return this.moduleManager;
    }

    @Override
    public I18n getI18n()
    {
        return this.i18n;
    }

    @Override
    public CubeLogger getCoreLogger()
    {
        return this.logger;
    }

    @Override
    public EventManager getEventManager()
    {
        return this.eventRegistration;
    }

    @Override
    public CoreConfiguration getConfiguration()
    {
        return this.config;
    }

    @Override
    public CommandManager getCommandManager()
    {
        return this.commandManager;
    }

    @Override
    public ScheduledExecutorService getExecutor()
    {
        return executor;
    }

    @Override
    public TableManager getTableManger()
    {
        return this.tableManager;
    }

    @Override
    public boolean isDebug()
    {
        if (this.config == null)
        {
            return true;
        }
        return this.config.debugMode;
    }
}