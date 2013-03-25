package de.cubeisland.cubeengine.vaultcompat;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.conomy.Conomy;

import net.milkbowl.vault.economy.Economy;

public class Vaultcompat extends Module
{
    @Override
    public void onEnable()
    {
        BukkitCore core = this.getCore();
        ServicesManager servicesManager = core.getServer().getServicesManager();

        final ModuleManager mm = core.getModuleManager();

        Module module = mm.getModule("conomy");
        if (module != null && module instanceof Conomy)
        {
            servicesManager.register(Economy.class, new VaultConomyService(this, (Conomy)module), core, ServicePriority.Highest);
        }

        module = mm.getModule("roles");
        if (module != null && module instanceof Conomy)
        {
            servicesManager.register(Economy.class, new VaultConomyService(this, (Conomy)module), core, ServicePriority.Highest);

            module = mm.getModule("chat");
            if (module != null && module instanceof Conomy)
            {
                servicesManager.register(Economy.class, new VaultConomyService(this, (Conomy)module), core, ServicePriority.Highest);
            }
        }
    }

    @Override
    public BukkitCore getCore()
    {
        return (BukkitCore)super.getCore();
    }
}
