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
package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.basics.command.general.ChatCommands;
import de.cubeisland.cubeengine.basics.command.general.ColoredSigns;
import de.cubeisland.cubeengine.basics.command.general.FlyListener;
import de.cubeisland.cubeengine.basics.command.general.GeneralsListener;
import de.cubeisland.cubeengine.basics.command.general.InformationCommands;
import de.cubeisland.cubeengine.basics.command.general.LagTimer;
import de.cubeisland.cubeengine.basics.command.general.ListCommand;
import de.cubeisland.cubeengine.basics.command.general.MuteListener;
import de.cubeisland.cubeengine.basics.command.general.PlayerCommands;
import de.cubeisland.cubeengine.basics.command.mail.MailCommand;
import de.cubeisland.cubeengine.basics.command.mail.MailManager;
import de.cubeisland.cubeengine.basics.command.moderation.DoorCommand;
import de.cubeisland.cubeengine.basics.command.moderation.InventoryCommands;
import de.cubeisland.cubeengine.basics.command.moderation.ItemCommands;
import de.cubeisland.cubeengine.basics.command.moderation.KickBanCommands;
import de.cubeisland.cubeengine.basics.command.moderation.PaintingListener;
import de.cubeisland.cubeengine.basics.command.moderation.PowerToolCommand;
import de.cubeisland.cubeengine.basics.command.moderation.TimeControlCommands;
import de.cubeisland.cubeengine.basics.command.moderation.WorldControlCommands;
import de.cubeisland.cubeengine.basics.command.moderation.kit.KitCommand;
import de.cubeisland.cubeengine.basics.command.moderation.kit.KitItem;
import de.cubeisland.cubeengine.basics.command.moderation.kit.KitItemConverter;
import de.cubeisland.cubeengine.basics.command.moderation.kit.KitManager;
import de.cubeisland.cubeengine.basics.command.moderation.kit.KitsGivenManager;
import de.cubeisland.cubeengine.basics.command.moderation.spawnmob.SpawnMobCommand;
import de.cubeisland.cubeengine.basics.command.teleport.MovementCommands;
import de.cubeisland.cubeengine.basics.command.teleport.SpawnCommands;
import de.cubeisland.cubeengine.basics.command.teleport.TeleportCommands;
import de.cubeisland.cubeengine.basics.command.teleport.TeleportListener;
import de.cubeisland.cubeengine.basics.command.teleport.TeleportRequestCommands;
import de.cubeisland.cubeengine.basics.command.teleport.TpWorldPermissions;
import de.cubeisland.cubeengine.basics.storage.BasicUserManager;
import de.cubeisland.cubeengine.basics.storage.IgnoreListManager;

public class Basics extends Module
{

    private BasicsConfiguration config;
    private BasicUserManager basicUM;
    private MailManager mailManager;
    private KitsGivenManager kitGivenManager;
    private IgnoreListManager ignoreListManager;
    private KitManager kitManager;
    private LagTimer lagTimer;
    private BasicsPerm perm;
    private TpWorldPermissions tpPerm;

    @Override
    public void onEnable()
    {
		final Database db = this.getCore().getDB();
        final CommandManager cm = this.getCore().getCommandManager();
        final EventManager em = this.getCore().getEventManager();
        this.basicUM = new BasicUserManager(this);
        this.mailManager = new MailManager(db, this.basicUM);
        this.ignoreListManager = new IgnoreListManager(db);
        this.perm = new BasicsPerm(this);
        this.getCore().getUserManager().addDefaultAttachment(BasicsAttachment.class, this);

        em.registerListener(this, new ColoredSigns());

        //General:
        cm.registerCommands(this, new ChatCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new InformationCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new ListCommand(this), ReflectedCommand.class);
        cm.registerCommand(new MailCommand(this));
        cm.registerCommands(this, new PlayerCommands(this), ReflectedCommand.class);
        em.registerListener(this, new GeneralsListener(this));
        em.registerListener(this, new MuteListener(this));

        //Moderation:
        cm.registerCommands(this, new InventoryCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new ItemCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new KickBanCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new SpawnMobCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new TimeControlCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new WorldControlCommands(this), ReflectedCommand.class);
        PowerToolCommand ptCommands = new PowerToolCommand(this);
        cm.registerCommand(ptCommands);
        em.registerListener(this, ptCommands);
        cm.registerCommand(new KitCommand(this));
        
        em.registerListener(this, new PaintingListener(this));

        Convert.registerConverter(KitItem.class, new KitItemConverter());

        this.kitManager = new KitManager(this);
        kitManager.loadKits();
        this.kitGivenManager = new KitsGivenManager(db);

        //Teleport:
        cm.registerCommands(this, new MovementCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new SpawnCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new TeleportCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new TeleportRequestCommands(this), ReflectedCommand.class);
        em.registerListener(this, new TeleportListener(this));
        em.registerListener(this, new FlyListener());
        this.tpPerm = new TpWorldPermissions(this); // per world permissions
        this.lagTimer = new LagTimer(this);

        cm.registerCommands(this,  new DoorCommand(this), ReflectedCommand.class );
        
        /**
         * * //commands TODO
         *
         * helpop -> move to CubePermissions ?? not only op but also "Moderator"
         * ignore -> move to CubeChat info
         *
         * nick -> move to CubeChat realname -> move to CubeChat rules
         *
         * help -> Display ALL availiable cmd
         */
    }

    @Override
    public void onDisable()
    {
        this.perm.cleanup();
        this.tpPerm.cleanup();
    }

    public BasicsConfiguration getConfiguration()
    {
        return this.config;
    }

    public BasicUserManager getBasicUserManager()
    {
        return this.basicUM;
    }

    public MailManager getMailManager()
    {
        return this.mailManager;
    }

    public KitsGivenManager getKitGivenManager()
    {
        return kitGivenManager;
    }

    public IgnoreListManager getIgnoreListManager()
    {
        return ignoreListManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public LagTimer getLagTimer() {
        return lagTimer;
    }
}