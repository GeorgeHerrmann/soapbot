package com.georgster.util.commands.wizard;

import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

public class PermissionsWizard extends InputWizard {
    private static final String TITLE = "Permissions Wizard";

    private PermissionsManager permissionsManager;

    public PermissionsWizard(CommandExecutionEvent event, PermissionsManager permissionsManager) {
        super (TITLE, event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.permissionsManager = permissionsManager;
    }

    private void managePermissions() {
        
    }
}
