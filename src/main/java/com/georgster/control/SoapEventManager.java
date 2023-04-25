package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventHandler;
import com.georgster.events.SoapEventType;
import com.georgster.profile.DatabaseService;
import com.georgster.profile.ProfileType;
import com.georgster.profile.adapter.DatabaseObjectDeserializer;
import com.georgster.profile.adapter.SoapEventDeserializer;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.core.object.entity.Guild;

/**
 * Manages all {@code SoapEvents} for a given {@code SoapClient}.
 * The {@code SoapEventManager} is responsible for interacting with the {@code ProfileHandler}
 * to add, remove, and update events to their profiles, and handles the scheduling of events
 * through the {@code SoapEventHandler}.
 */
public class SoapEventManager {
    private static final DatabaseObjectDeserializer<SoapEvent> eventDeserializer = new SoapEventDeserializer();
    private final List<SoapEvent> events;//The List of events this manager is observing
    private final GuildInteractionHandler handler;
    private final DatabaseService<SoapEvent> dbService;

    /**
     * Constructs a {@code SoapEventManager} for the given {@code Guild}
     * controlled by a {@code SoapClient}.
     * 
     * @param guild the guild to manage events for
     */
    public SoapEventManager(Guild guild) {
        this.handler = new GuildInteractionHandler(guild);
        this.events = new ArrayList<>();

        this.dbService = new DatabaseService<>(handler.getId(), ProfileType.EVENTS, SoapEvent.class);
    }

    /**
     * Restarts all previously scheduled events this manager was oberserving.
     */
    public void restartEvents() {
        dbService.getAllObjects(eventDeserializer).forEach(event -> {
            events.add(event);
            ThreadPoolFactory.scheduleEventTask(handler.getId(), () -> SoapEventHandler.scheduleEvent(event, this));
        });
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
            dbService.addObjectIfNotExists(event, "identifier", event.getIdentifier(), eventDeserializer);
            ThreadPoolFactory.scheduleEventTask(handler.getId(), () -> SoapEventHandler.scheduleEvent(event, this));
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
        dbService.removeObjectIfExists("identifier", event.getIdentifier(), eventDeserializer);
    }

    /**
     * Removes all events from this manager and the server profile.
     * Once removed, the events will no longer be scheduled.
     * If no events are active, nothing happens.
     */
    public void removeAllEvents() {
        events.forEach(this::removeEvent);
    }

    /**
     * Updates the given event in this manager and the server profile.
     * 
     * @param event the event to update
     */
    public void updateEvent(SoapEvent event) {
        events.forEach(examiner -> {
            if (examiner.getIdentifier().equals(event.getIdentifier())) {
                events.set(events.indexOf(examiner), event); //Replace the old event with the new one
                dbService.updateObjectIfExists(event, "identifier", event.getIdentifier(), eventDeserializer);
                return;
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
     * Returns the number of events this manager is observing.
     * 
     * @return the number of events this manager is observing
     */
    public int getEventCount() {
        return events.size();
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
     * Returns whether or not an event with the given identifier and type exists.
     * 
     * @param identifier the identifier of the event to check for
     * @param type the type of the event to check for
     * @return true if an event with the given identifier and type exists, false otherwise
     */
    public boolean eventExists(String identifier, SoapEventType type)  {
        return events.stream().anyMatch(event -> event.getIdentifier().equals(identifier) && event.getType().equals(type));
    }

    /**
     * Returns whether or not this manager has any events.
     * 
     * @return true if this manager has any events, false otherwise
     */
    public boolean areEvents() {
        return !events.isEmpty();
    }

    /**
     * Returns whether or not this manager has any events of the given type.
     * 
     * @param type the type of events to check for
     * @return true if this manager has any events of the given type, false otherwise
     */
    public boolean areEvents(SoapEventType type) {
        return events.stream().anyMatch(event -> event.getType().equals(type));
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
     * Returns whether or not the given event exists in this manager with the given type.
     * 
     * @param event the event to check for
     * @param type the type of the event to check for
     * @return true if the given event exists in this manager with the given type, false otherwise
     */
    public boolean eventExists(SoapEvent event, SoapEventType type) {
        for (SoapEvent examiner : events) {
            if (event.same(examiner) && examiner.getType().equals(type)) {
                return true;
            }
        }
        return false;
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
     * Returns a list of all events of the given type in this manager.
     * 
     * @param type the type of events to return
     * @return a list of all events of the given type in this manager
     */
    public List<SoapEvent> getEvents(SoapEventType type) {
        List<SoapEvent> typeEvents = new ArrayList<>();
        events.forEach(event -> {
            if (event.getType() == type) {
                typeEvents.add(event);
            }
        });
        return typeEvents;
    }

    /**
     * Returns the {@code Guild} this manager is managing events for.
     * 
     * @return the guild this manager is managing events for
     */
    public Guild getGuild() {
        return handler.getGuild();
    }
}
