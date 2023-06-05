package com.georgster.util.commands.wizard;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

public class ReserveEventWizard extends InputWizard {
    private static final String TITLE = "Reserve Event Wizard";

    private final SoapEventManager eventManager;

    public ReserveEventWizard(CommandExecutionEvent event, SoapEventManager eventManager) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = eventManager;
    }
}
