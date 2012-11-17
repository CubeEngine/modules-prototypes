package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.util.Pair;

import java.util.Queue;

public interface AnnouncementReceiver
{
    public String getName();

    public void sendMessage(String message);

    public String getWorld();

    public Pair<Announcement, Integer> getNextDelayAndAnnouncement();

    public Queue<Announcement> getAllAnnouncements();

    public String getLanguage();

    public void setAllAnnouncements(Queue<Announcement> announcements);

    public void setWorld(String world);
}