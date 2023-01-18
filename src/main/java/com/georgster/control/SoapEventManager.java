package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventHandler;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.GuildManager;

import discord4j.core.object.entity.Guild;

/**
 * Manages all {@code SoapEvents} for a given {@code SoapClient}.
 * The {@code SoapEventManager} is responsible for interacting with the {@code ProfileHandler}
 * to add, remove, and update events to their profiles, and handles the scheduling of events
 * through the {@code SoapEventHandler}.
 */
public class SoapEventManager {
    private final List<SoapEvent> events = new ArrayList<>(); //The List of events this manager is observing
    private final GuildManager manager; //The GuildManager for the guild this manager is managing events for
    private final ProfileHandler profileHandler; //The ProfileHandler for the guild this manager is managing events for

    /**
     * Constructs a {@code SoapEventManager} for the given {@code Guild}
     * controlled by a {@code SoapClient}.
     * 
     * @param guild the guild to manage events for
     */
    public SoapEventManager(Guild guild) {
        this.manager = new GuildManager(guild);
        this.profileHandler = manager.getProfileHandler();
    }

    /**
     * Adds the given event to this manager and the server profile.
     * Once added, the event will be scheduled.
     * 
     * @param event the event to add
     */
    public void addEvent(SoapEvent event) {
        if (!eventExists(event)) {
            events.add(event);
            profileHandler.addObject(event, ProfileType.EVENTS);
            SoapEventHandler.scheduleEvent(event, manager);
        }
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

    /**
     * Updates the given event in this manager and the server profile.
     * 
     * @param event the event to update
     */
    public void updateEvent(SoapEvent event) {
        events.forEach(examiner -> {
            if (examiner.getIdentifier().equals(event.getIdentifier())) {
                profileHandler.removeObject(examiner, ProfileType.EVENTS);
                events.set(events.indexOf(examiner), event); //Replace the old event with the new one
                profileHandler.addObject(event, ProfileType.EVENTS);
            }
        });
    }

    /**
     * Returns the event with the given identifier.
     * 
     * @param identifier the identifier of the event to return
     * @return the event with the given identifier, or null if no event with the given identifier exists
     */
    public SoapEvent getEvent(String identifier) {
        return events.stream().filter(event -> event.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    /**
     * Returns whether or not an event with the given identifier exists.
     * 
     * @param identifier the identifier of the event to check for
     * @return true if an event with the given identifier exists, false otherwise
     */
    public boolean eventExists(String identifier) {
        return events.stream().anyMatch(event -> event.getIdentifier().equals(identifier));
    }

    /**
     * Returns whether or not the given event exists exactly in this manager,
     * meaning every attribute of the event is the same as an event in this manager.
     * 
     * @param event the event to check for
     * @return true if the given event exists exactly in this manager, false otherwise
     */
    public boolean eventExists(SoapEvent event) {
        for (SoapEvent examiner : events) {
            if (event.same(examiner)) {
                return true;
            }
        }
        return false;
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

    /**
     * Returns a list of all events in this manager.
     * 
     * @return a list of all events in this manager
     */
    public List<SoapEvent> getEvents() {
        return events;
    }

    /**
     * Returns the {@code Guild} this manager is managing events for.
     * 
     * @return the guild this manager is managing events for
     */
    public Guild getGuild() {
        return manager.getGuild();
    }
}
