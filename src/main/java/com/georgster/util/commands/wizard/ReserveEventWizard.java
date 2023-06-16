package com.georgster.util.commands.wizard;

import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.reserve.ReserveEvent;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

/**
 * An {@link InputWizard} for managing {@code ReserveEvents}.
 */
public class ReserveEventWizard extends InputWizard {
    private static final String TITLE = "Reserve Event Wizard";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private final SoapEventManager eventManager;

    /**
     * Creates a new ReserveEventWizard.
     * 
     * @param event The event that triggered the creation of this wizard.
     * @param eventManager The event manager for the SoapClient's Guild.
     */
    public ReserveEventWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = event.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        handler.sendText("Welcome to the reserve event wizard. At any time you can type \"stop\", or react :x: to exit the wizard", TITLE);
        nextWindow("manageEvents");
        end();
    }

    /**
     * The main screen for the event wizard, prompting the user to manage all ReserveEvents.
     */
    protected void manageEvents() {
        String prompt = "Which Reserve event would you like to manage?";
        String[] options = new String[eventManager.getCount()];
        List<SoapEvent> events = eventManager.getAll(TYPE);
        for (int i = 0; i < eventManager.getCount(); i++) {
            options[i] = events.get(i).getIdentifier();
        }

        withResponse((response -> 
            nextWindow("manageEvent", ((ReserveEvent) eventManager.get(response)))
        ), prompt, options);
    }

    /**
     * Screen for managing a specific ReserveEvent.
     * 
     * @param event The ReserveEvent to manage
     */
    protected void manageEvent(ReserveEvent event) {
        String prompt = "What would you like to edit about reserve event " + event.getIdentifier() + " ?";
        String[] options = {"number of people", "time", "date", "delete event"};
        withResponseBack((response -> {
            if (response.equals("number of people")) {
                nextWindow("editNumPeople", event);
            } else if (response.equals("time")) {
                nextWindow("editTime", event);
            } else if (response.equals("date")) {
                nextWindow("editDate", event);
            } else if (response.equals("delete event")) {
                eventManager.remove(event);
                sendMessage("Removed reserve event " + event.getIdentifier(), TITLE);
                nextWindow("manageEvents");
            }
        }), prompt, options);
    }

    /**
     * Screen for editing the number of people for a reserve event.
     * 
     * @param event The ReserveEvent to manage
     */
    protected void editNumPeople(ReserveEvent event) {
        String prompt = "Current max number of reservees: " + event.getNumPeople() + "\n" + 
                    "Please enter the new maximum number of people that can reserve to " + event.getIdentifier();

        withResponseBack((response -> {
            try {
                event.setNumPeople(Integer.parseInt(response));
                eventManager.update(event);
                sendMessage("Updated event " + event.getIdentifier() + " to allow " + response + " reservees.", TITLE);
            }  catch (NumberFormatException e) {
                sendMessage("That is not a number, please enter a number", TITLE);
            } catch (IllegalArgumentException e) {
                sendMessage(e.getMessage(), TITLE);
            }
        }), prompt);
    }

    /**
     * Screen for editing the time for a reserve event.
     * 
     * @param event The ReserveEvent to manage
     */
    protected void editTime(ReserveEvent event) {
        String prompt = "Current time: " + SoapUtility.convertToAmPm(event.getTime()) + "\n" + 
                    "Please enter the new time for " + event.getIdentifier();

        withResponseBack((response -> {
            try {
                event.setTime(response);
                eventManager.update(event);
                sendMessage("Updated event " + event.getIdentifier() + " to have the time " + SoapUtility.convertToAmPm(event.getTime()) + "." +
                                "\n*Note: The date may have been adjusted if the new date time was in the past*", TITLE);
            } catch (IllegalArgumentException e) {
                sendMessage(e.getMessage(), TITLE);
            }
        }), prompt);
    }

    /**
     * Screen for editing the date for a reserve event.
     * 
     * @param event The ReserveEvent to manage
     */
    protected void editDate(ReserveEvent event) {
        String prompt = "Current date: " + SoapUtility.formatDate(event.getDate()) + "\n" + 
                    "Please enter the new date for " + event.getIdentifier();

        withResponse((response -> {
            try {
                event.setDate(response);
                eventManager.update(event);
                sendMessage("Updated event " + event.getIdentifier() + " to have the date " + SoapUtility.formatDate(event.getDate()) + ".", TITLE);
            } catch (IllegalArgumentException e) {
                sendMessage(e.getMessage(), TITLE);
            }
        }), prompt);
    }
}
