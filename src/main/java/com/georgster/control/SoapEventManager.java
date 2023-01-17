package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.events.SoapEvent;
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

    public void removeEvent(SoapEvent event) {
        events.remove(event);
        profileHandler.removeObject(event, ProfileType.EVENTS);
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

    public boolean matchesServerProfile() {
        List<SoapEvent> profileEvents = profileHandler.getEvents();
        for (SoapEvent event : events) {
            if (!profileEvents.contains(event)) {
                return false;
            }
        }
    }

    public List<SoapEvent> getEvents() {
        return events;
    }

    public Guild getGuild() {
        return manager.getGuild();
    }
}
