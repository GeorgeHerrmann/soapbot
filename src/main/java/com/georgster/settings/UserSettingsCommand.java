package com.georgster.settings;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.UserSettingsWizard;

public final class UserSettingsCommand implements Command {
    
    public void execute(CommandExecutionEvent event) {
        new UserSettingsWizard(event).begin();
    }

    public List<String> getAliases() {
        return List.of("settings", "set");
    }

    public String help() {
        return "Change your user settings.";
    }

}
