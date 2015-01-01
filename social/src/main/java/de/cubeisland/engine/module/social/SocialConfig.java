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
package de.cubeisland.engine.module.social;

import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.codec.yaml.ReflectedYaml;

public class SocialConfig extends ReflectedYaml
{
    @Comment("The application key for your facebook application")
    public String key = "";

    @Comment("The application secret for your facebook application")
    public String secret = "";

    @Comment("This should be the same as the server address + /callback")
    public String callbackUrl = "";

    @Override
    public String[] head()
    {
        return new String[] {
            "The global config for CubeSocial",
            "To get a Facebook app key and secret you have to register a facebook application",
            "this can be done here: https://developers.facebook.com/apps"
        };
    }
}
