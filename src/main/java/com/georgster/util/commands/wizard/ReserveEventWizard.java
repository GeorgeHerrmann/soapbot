package com.georgster.util.commands.wizard;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.reserve.ReserveEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

public class ReserveEventWizard extends InputWizard {
    private static final String TITLE = "Reserve Event Wizard";
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private final SoapEventManager eventManager;

    public ReserveEventWizard(CommandExecutionEvent event, SoapEventManager eventManager) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = eventManager;
    }

    public void begin() {
        handler.sendText("Welcome to the reserve event wizard. At any time you can type \"stop\", or react :x: to exit the wizard");
        manageEvents();
        end();
    }

    private void manageEvents() {
        while (isActive()) {
            String prompt = "Which Reserve event would you like to manage?";
            String[] optionsArr = new String[eventManager.getCount()];
            String[] options = eventManager.getAll(TYPE).toArray(optionsArr);

            withResponseFirst((response -> {
                manageEvent((ReserveEvent)eventManager.get(response));
            }), prompt, options);
        }
    }

    private void manageEvent(ReserveEvent event) {
        while (isActive()) {
            String prompt = "What would you like to edit about reserve event " + event.getIdentifier() + " ?";
            String options = {"name", "number of people", "time", "date", "delete event"};
            WizardResponse wizardResponse = withResponse((response -> {
                if (response.equals("name")) {
                    editName(event)
                } else if (response.equals("number of people")) {
                    
                } else if (response.equals("time")) {

                } else if (response.equals("date")) {
                    
                } else if (response.equals("delete event")) {

                }
            }), options, null)
        }
    }
}
