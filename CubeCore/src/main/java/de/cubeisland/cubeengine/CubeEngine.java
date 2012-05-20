package de.cubeisland.cubeengine;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.user.UserManager;

/**
 *
 * @author CodeInfection
 */
public final class CubeEngine
{
    private static CubeCore core = null;

    private CubeEngine()
    {}

    public static void initialize(CubeCore coreModule)
    {
        if (core == null)
        {
            if (coreModule == null)
            {
                throw new IllegalArgumentException("The core module must not be null!");
            }
            core = coreModule;
        }
    }

    public static void clean()
    {
        core = null;
    }

    public static CubeCore getCore()
    {
        return core;
    }

    public static Database getDatabase()
    {
        return core.getDB();
    }

    public static PermissionRegistration getPermissionRegistration()
    {
        return core.getPermissionRegistration();
    }

    public static void registerPermissions(Permission[] permissions)
    {
        getPermissionRegistration().registerPermissions(permissions);
    }

    public static UserManager getUserManager()
    {
        return core.getUserManager();
    }

    public static FileManager getFileManager()
    {
        return core.getFileManager();
    }
}
