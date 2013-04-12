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
package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.roles.config.RoleMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Codec("yml")
@DefaultConfig
public class RolesConfig extends Configuration
{
    @Option("disable-permission-in-offlinemode")
    @Comment("If this is set to true no permissions will be assigned to any user if the server runs in offline-mode")
    public boolean doNotAssignPermIfOffline = true;
    @Option("default.roles")
    @Comment("The list of roles a user will get when first joining the server.")
    public Map<String, List<String>> defaultRoles = new HashMap<String, List<String>>(); //TODO example in comment
    @Option("mirrors")
    public List<RoleMirror> mirrors = new ArrayList<RoleMirror>(); //TODO example in comment
}