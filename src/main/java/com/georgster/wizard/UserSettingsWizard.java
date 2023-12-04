package com.georgster.wizard;

import com.georgster.control.manager.UserSettingsManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.settings.UserSettings;
import com.georgster.settings.UserSettings.SettingsOption;
import com.georgster.wizard.input.InputListenerFactory;

public final class UserSettingsWizard extends InputWizard {
    
    private final UserSettingsManager manager;

    public UserSettingsWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, "User Settings Manager"));
        this.manager = event.getClientContext().getUserSettingsManager();
    }

    protected void selectSetting() {
        final String prompt = "Which setting would you like to change?";
        String[] options = manager.get(user.getId().asString()).getAllSettings().stream()
                .map(SettingsOption::name).toArray(String[]::new);

        withResponse(response -> {
            try {
                SettingsOption option = manager.get(user.getId().asString()).getSetting(response);
            } catch (Exception e) {
                sendMessage("Sorry, that setting does not exist. Try again.", "Invalid Setting");
            }
        }, false, prompt, options);
    }

    protected void selectOption(UserSettings SettingsOption option) {
        final String prompt = "Which option would you like to set?";
        String[] options = option.availableOptions().toArray(String[]::new);

        withResponse(response -> {
            try {
                option.setOption(response);
                manager.update()
            } catch (Exception e) {
                sendMessage("Sorry, that option does not exist. Try again.", "Invalid Option");
            }
        }, false, prompt, options);
    }

}
