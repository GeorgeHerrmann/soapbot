package com.georgster.wizard;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.input.InputListenerFactory;

public final class CollectableWizard extends InputWizard {
    public CollectableWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, "Collectable Wizard"));
    }
}
