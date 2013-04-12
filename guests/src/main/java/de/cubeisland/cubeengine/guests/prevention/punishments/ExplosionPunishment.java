/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Creates an explosion on the player's position.
 */
public class ExplosionPunishment implements Punishment
{
    private final Location helper = new Location(null, 0, 0, 0);

    @Override
    public String getName()
    {
        return "explosion";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.getWorld().createExplosion(player.getLocation(this.helper), 0);
        player.damage(config.getInt("damage", 3));
    }
}