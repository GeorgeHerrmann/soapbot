package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.database.adapter.SoapEventClassAdapter;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import com.georgster.util.thread.ThreadPoolFactory;

/**
 * Manages and schedules all {@link SoapEvent SoapEvents} for a given {@code SoapClient}.
 */
public class SoapEventManager extends AbstractGuildedSoapManager<SoapEvent> {

    /**
     * Constructs a {@link SoapEventManager} for the given {@code ClientContext}.
     * 
     * @param context The context of the client controlling the guild
     */
    public SoapEventManager(ClientContext context) {
        super(context, ProfileType.EVENTS, SoapEvent.class, "identifier", new SoapEventClassAdapter());
    }

    /**
     * Restarts all previously scheduled events this manager was oberserving.
     */
    @Override
    public void load() {
        dbService.getAllObjects(adapter).forEach(event -> {
            if (!exists(event)) {
                observees.add(event);
                ThreadPoolFactory.scheduleEventTask(handler.getId(), () -> this.scheduleEvent(event));
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
            ThreadPoolFactory.scheduleEventTask(handler.getId(), () -> this.scheduleEvent(event));
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

    /**
     * Schedules a {@code SoapEvent} to be fulfilled for the Guild the event originated from.
     * If the event is a {@code ReserveEvent} the active channel is set to the channel the event was reserved in.
     * This method will wait and block the calling thread until the event has been
     * cancelled, is no longer valid, or the event's {@code fulfilled()} condition has been met and will
     * call {@code onFulfill()} on the event if the event is valid.
     * 
     * @param event the event to be scheduled
     */
    public void scheduleEvent(final SoapEvent event) {
        if (this.exists(event.getIdentifier())) {
            handler.setActiveMessageChannel(handler.getMessageChannel(event.getChannel()));
            if (validateSoapEvent(event)) {
                event.onFulfill(handler);
            } else {
                handler.sendMessage("Event " + event.getIdentifier() + " has been cancelled", "Event Cancelled", MessageFormatting.INFO);
            }
            if (this.exists(event.getIdentifier())) {
                this.remove(event);
            }
        }
    }

    
    /**
     * Validates a {@code SoapEvent} by waiting until the event has been cancelled, is no longer valid
     * or the event's {@code fulfilled()} condition has been met.
     * 
     * @param event the event to be validated
     * @return true if the event is valid, false otherwise
     */
    private boolean validateSoapEvent(final SoapEvent event) {
        try {
            /* Will continue to wait until the event has been removed/cancelled, or the fulfillment condition has been met */
            while (this.exists(event.getIdentifier()) && !event.fulfilled()) {
                Thread.sleep(2000);
            }
            if (!this.exists(event.getIdentifier())) { //If it was removed we return false
                return false;
            }
        } catch (Exception e) { //If we are interrupted we return false
            Thread.currentThread().interrupt();
            return false;
        }
        return true; //Otherwise we return true
    }
}
