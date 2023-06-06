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
    public ReserveEventWizard(CommandExecutionEvent event, SoapEventManager eventManager) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = eventManager;
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        handler.sendText("Welcome to the reserve event wizard. At any time you can type \"stop\", or react :x: to exit the wizard");
        manageEvents();
        end();
    }

    /**
     * The main screen for the event wizard, prompting the user to manage all ReserveEvents.
     */
    private void manageEvents() {
        while (isActive()) {
            String prompt = "Which Reserve event would you like to manage?";
            String[] options = new String[eventManager.getCount()];
            List<SoapEvent> events = eventManager.getAll(TYPE);
            for (int i = 0; i < eventManager.getCount(); i++) {
                options[i] = events.get(i).getIdentifier();
            }

            withResponseFirst((response -> 
                manageEvent((ReserveEvent) eventManager.get(response))
            ), prompt, options);
        }
    }

    /**
     * Screen for managing a specific ReserveEvent.
     * 
     * @param event The ReserveEvent to manage
     */
    private void manageEvent(ReserveEvent event) {
        while (isActive()) {
            String prompt = "What would you like to edit about reserve event " + event.getIdentifier() + " ?";
            String[] options = {"number of people", "time", "date", "delete event"};
            WizardResponse wizardResponse = withResponse((response -> {
                if (response.equals("number of people")) {
                    editNumPeople(event);
                } else if (response.equals("time")) {
                    editTime(event);
                } else if (response.equals("date")) {
                    editDate(event);
                } else if (response.equals("delete event")) {
                    eventManager.remove(event);
                    handler.sendText("Removed reserve event " + event.getIdentifier(), TITLE);
                    manageEvents();
                }
            }), prompt, options);
            if (wizardResponse == WizardResponse.BACK) {
                return;
            }
        }
    }

    /**
     * Screen for editing the number of people for a reserve event.
     * 
     * @param event The ReserveEvent to manage
     */
    private void editNumPeople(ReserveEvent event) {
        while (isActive()) {
            String prompt = "Current max number of reservees: " + event.getNumPeople() + "\n" + 
                        "Please enter the new maximum number of people that can reserve to " + event.getIdentifier();

            WizardResponse wizardResponse = withResponse((response -> {
                try {
                    event.setNumPeople(Integer.parseInt(response));
                    eventManager.update(event);
                    handler.sendText("Updated event " + event.getIdentifier() + " to allow " + response + " reservees.", TITLE);
                }  catch (NumberFormatException e) {
                    handler.sendText("That is not a number, please enter a number", TITLE);
                } catch (IllegalArgumentException e) {
                    handler.sendText(e.getMessage(), TITLE);
                }
            }), prompt);
            if (wizardResponse == WizardResponse.BACK) {
                return;
            }
        }
    }

    /**
     * Screen for editing the time for a reserve event.
     * 
     * @param event The ReserveEvent to manage
     */
    private void editTime(ReserveEvent event) {
        while (isActive()) {
            String prompt = "Current time: " + SoapUtility.convertToAmPm(event.getTime()) + "\n" + 
                        "Please enter the new time for " + event.getIdentifier();

            WizardResponse wizardResponse = withResponse((response -> {
                try {
                    event.setTime(response);
                    eventManager.update(event);
                    handler.sendText("Updated event " + event.getIdentifier() + " to have the time " + SoapUtility.convertToAmPm(event.getTime()) + "." +
                                    "\n*Note: The date may have been adjusted if the new date time was in the past*", TITLE);
                } catch (IllegalArgumentException e) {
                    handler.sendText(e.getMessage(), TITLE);
                }
            }), prompt);
            if (wizardResponse == WizardResponse.BACK) {
                return;
            }
        }
    }

    /**
     * Screen for editing the date for a reserve event.
     * 
     * @param event The ReserveEvent to manage
     */
    private void editDate(ReserveEvent event) {
        while (isActive()) {
            String prompt = "Current date: " + SoapUtility.formatDate(event.getDate()) + "\n" + 
                        "Please enter the new date for " + event.getIdentifier();

            WizardResponse wizardResponse = withResponse((response -> {
                try {
                    event.setDate(response);
                    eventManager.update(event);
                    handler.sendText("Updated event " + event.getIdentifier() + " to have the date " + SoapUtility.formatDate(event.getDate()) + ".", TITLE);
                } catch (IllegalArgumentException e) {
                    handler.sendText(e.getMessage(), TITLE);
                }
            }), prompt);
            if (wizardResponse == WizardResponse.BACK) {
                return;
            }
        }
    }
}
