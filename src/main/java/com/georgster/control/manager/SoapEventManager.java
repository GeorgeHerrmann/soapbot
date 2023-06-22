package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.database.adapter.SoapEventClassAdapter;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventHandler;
import com.georgster.events.SoapEventType;
import com.georgster.util.thread.ThreadPoolFactory;

/**
 * Manages all {@code SoapEvents} for a given {@code SoapClient} and
 * handles the scheduling of events through the {@code SoapEventHandler}.
 */
public class SoapEventManager extends AbstractSoapManager<SoapEvent> {

    /**
     * Constructs a {@code SoapEventManager} for the given {@code Guild}
     * controlled by a {@code SoapClient}.
     * 
     * @param context the context of the client controlling the guild
     */
    public SoapEventManager(ClientContext context) {
        super(context, ProfileType.EVENTS, SoapEvent.class, "identifier", new SoapEventClassAdapter());
    }

    /**
     * Restarts all previously scheduled events this manager was oberserving.
     */
    public void restartEvents() {
        dbService.getAllObjects(adapter).forEach(event -> {
            if (!exists(event)) {
                observees.add(event);
                ThreadPoolFactory.scheduleEventTask(handler.getId(), () -> SoapEventHandler.scheduleEvent(event, this));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(SoapEvent event) {
        if (!exists(event)) {
            observees.add(event);
            dbService.addObjectIfNotExists(event, "identifier", event.getIdentifier(), adapter);
            ThreadPoolFactory.scheduleEventTask(handler.getId(), () -> SoapEventHandler.scheduleEvent(event, this));
        }
    }

    /**
     * Returns whether or not an event with the given identifier and type exists.
     * 
     * @param identifier the identifier of the event to check for
     * @param type the type of the event to check for
     * @return true if an event with the given identifier and type exists, false otherwise
     */
    public boolean exists(String identifier, SoapEventType type)  {
        return observees.stream().anyMatch(event -> event.getIdentifier().equals(identifier) && event.getType().equals(type));
    }

    /**
     * Returns whether or not this manager has any events of the given type.
     * 
     * @param type the type of events to check for
     * @return true if this manager has any events of the given type, false otherwise
     */
    public boolean hasAny(SoapEventType type) {
        return observees.stream().anyMatch(event -> event.getType().equals(type));
    }

    /**
     * Returns the number of events of the given type in this manager.
     * 
     * @param type The type to compare with.
     * @return The number of events of the given type.
     */
    public int getCount(SoapEventType type) {
        return (int) observees.stream().filter(event -> event.getType() == type).count();
    }

    /**
     * Returns whether or not the given event exists in this manager with the given type.
     * 
     * @param event the event to check for
     * @param type the type of the event to check for
     * @return true if the given event exists in this manager with the given type, false otherwise
     */
    public boolean exists(SoapEvent event, SoapEventType type) {
        for (SoapEvent examiner : observees) {
            if (event.same(examiner) && examiner.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all events of the given type in this manager.
     * 
     * @param type the type of events to return
     * @return a list of all events of the given type in this manager
     */
    public List<SoapEvent> getAll(SoapEventType type) {
        List<SoapEvent> typeEvents = new ArrayList<>();
        observees.forEach(event -> {
            if (event.getType() == type) {
                typeEvents.add(event);
            }
        });
        return typeEvents;
    }
}
