package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

/**
 * Prevents vehicle usage
 *
 * @author Phillip Schichtel
 */
public class VehiclePrevention extends Prevention
{
    private boolean access;
    private boolean destruction;
    private boolean collision;
    private boolean creation;

    public VehiclePrevention(Guests guests)
    {
        super("vehicle", guests);
        setThrottleDelay(3);
    }

    @Override
    public void enable()
    {
        super.enable();

        this.access      = getConfig().getBoolean("prevent.access");
        this.destruction = getConfig().getBoolean("prevent.destruction");
        this.collision   = getConfig().getBoolean("prevent.collision");
        this.creation    = getConfig().getBoolean("prevent.creation");
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration defaultConfig = super.getDefaultConfig();

        defaultConfig.set("prevent.access", true);
        defaultConfig.set("prevent.destruction", true);
        defaultConfig.set("prevent.collision", true);
        defaultConfig.set("prevent.creation", true);

        return defaultConfig;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void enter(VehicleEnterEvent event)
    {
        if (!this.access)
        {
            return;
        }
        final Entity entered = event.getEntered();
        if (entered instanceof Player)
        {
            prevent(event, (Player)entered);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void destroy(VehicleDestroyEvent event)
    {
        if (!this.destruction)
        {
            return;
        }
        final Entity attacker = event.getAttacker();
        if (attacker instanceof Player)
        {
            prevent(event, (Player)attacker);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityCollision(VehicleEntityCollisionEvent event)
    {
        if (!this.collision)
        {
            return;
        }
        final Entity collider = event.getEntity();
        if (collider instanceof Player)
        {
            if (prevent(event, (Player)collider))
            {
                event.setCollisionCancelled(true);
                event.setPickupCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event)
    {
        if (!this.creation)
        {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            final Material clickedMaterial = event.getClickedBlock().getType();
            final Player player = event.getPlayer();
            final Material materialInHand = player.getItemInHand().getType();
            if (clickedMaterial == Material.RAILS || clickedMaterial == Material.POWERED_RAIL || clickedMaterial == Material.DETECTOR_RAIL)
            {
                if (materialInHand == Material.MINECART || materialInHand == Material.POWERED_MINECART || materialInHand == Material.STORAGE_MINECART)
                {
                    prevent(event, player);
                }
            }
            else
            {
                if (materialInHand == Material.BOAT)
                {
                    prevent(event, player);
                }
            }
        }
    }    
}