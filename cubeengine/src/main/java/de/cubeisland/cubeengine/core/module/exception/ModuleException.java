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
package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is fired when an error occurs during the loading-process of modules.
 */
public class ModuleException extends Exception
{
    public ModuleException()
    {
        super();
    }

    public ModuleException(String message)
    {
        super(message);
    }

    public ModuleException(Throwable cause)
    {
        super(cause);
    }

    public ModuleException(String message, Throwable cause)
    {
        super(message, cause);
    }
}