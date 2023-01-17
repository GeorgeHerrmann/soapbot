package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventHandler;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.GuildManager;

import discord4j.core.object.entity.Guild;

public class SoapEventManager {
    private final List<SoapEvent> events = new ArrayList<>();
    private final GuildManager manager;
    private final ProfileHandler profileHandler;

    public SoapEventManager(Guild guild) {
        this.manager = new GuildManager(guild);
        this.profileHandler = manager.getProfileHandler();
    }

    public void addEvent(SoapEvent event) {
        events.add(event);
        profileHandler.addObject(event, ProfileType.EVENTS);
    }

    /**
     * Removes the given event from this manager and the server profile.
     * Once removed, the event will no longer be scheduled.
     * 
     * @param event the event to remove
     */
    public void removeEvent(SoapEvent event) {
        events.remove(event);
        profileHandler.removeObject(event, ProfileType.EVENTS);
    }

    /**
     * Removes all events from this manager and the server profile.
     * Once removed, the events will no longer be scheduled.
     * If no events are active, nothing happens.
     */
    public void removeAllEvents() {
        events.forEach(event -> profileHandler.removeObject(event, ProfileType.EVENTS));
        events.clear();
    }

    public void updateEvent(SoapEvent event) {
        events.forEach(examiner -> {
            if (examiner.getIdentifier().equals(event.getIdentifier())) {
                profileHandler.removeObject(examiner, ProfileType.EVENTS);
                events.set(events.indexOf(examiner), event); //Replace the old event with the new one
                profileHandler.addObject(event, ProfileType.EVENTS);
            }
        });
    }

    public SoapEvent getEvent(String identifier) {
        return events.stream().filter(event -> event.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public boolean eventExists(String identifier) {
        return events.stream().anyMatch(event -> event.getIdentifier().equals(identifier));
    }

    public boolean eventExists(SoapEvent event) {
        return events.contains(event);
    }

    /**
     * Checks if the events in this manager match the events in the server profile.
     * 
     * @return true if each event in this manager is in and matches exactly the event in the server profile, false otherwise.
     */
    public boolean matchesServerProfile() {
        List<SoapEvent> profileEvents = profileHandler.getEvents();
        int totalEvents = 0;
        for (SoapEvent event : events) {
            if (!profileEvents.contains(event)) {
                return false;
            }
            totalEvents++;
        }
        return totalEvents == profileEvents.size();
    }

    public void scheduleAllEvents() {
        events.forEach(event -> SoapEventHandler.scheduleEvent(event, manager));
    }

    public void scheduleEvent(SoapEvent event) {
        SoapEventHandler.scheduleEvent(event, manager);
    }

    public List<SoapEvent> getEvents() {
        return events;
    }

    public Guild getGuild() {
        return manager.getGuild();
    }
}
