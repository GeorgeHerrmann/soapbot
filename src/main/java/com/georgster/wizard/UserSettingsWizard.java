package com.georgster.wizard;

import com.georgster.control.manager.UserSettingsManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.settings.TimezoneOption;
import com.georgster.settings.UserSettings;
import com.georgster.settings.UserSettings.SettingsOption;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * An {@link InputWizard} that allows the user to change their {@link UserSettings}.
 */
public final class UserSettingsWizard extends InputWizard {
    
    private final UserSettingsManager manager;
    private final UserSettings settings;
    private SettingsOption changingOption;

    /**
     * Creates a new {@link UserSettingsWizard} with the given {@link CommandExecutionEvent}.
     * 
     * @param event The {@link CommandExecutionEvent} to create the {@link UserSettingsWizard} with.
     */
    public UserSettingsWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, "User Settings Manager"));
        this.manager = event.getClientContext().getUserSettingsManager();
        this.settings = manager.get(user.getId().asString());
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("selectSetting");
        end();
    }

    /**
     * The select setting window.
     */
    protected void selectSetting() {

        final String prompt = "Which setting would you like to change?";
        String[] options = settings.getAllSettings().stream()
                .map(SettingsOption::name).toArray(String[]::new);

        withResponse(response -> {
            try {
                this.changingOption = settings.getSetting(response);
                nextWindow("selectOption");
            } catch (Exception e) {
                sendMessage("Sorry, that setting does not exist. Try again.", "Invalid Setting");
            }
        }, false, prompt, options);
    }

    /**
     * The select option window.
     */
    protected void selectOption() {
        final String prompt = "Which option would you like to set? *(Current: " + getSettingDisplay(changingOption) + ")*";
        String[] options = changingOption.availableOptions().toArray(String[]::new);

        withResponse(response -> {
            try {
                changingOption.setOption(response);
                manager.update(settings);
                sendMessage("Successfully changed setting to " + getSettingDisplay(changingOption), "Success");
            } catch (Exception e) {
                sendMessage("Sorry, that option does not exist. Try again.", "Invalid Option");
            }
        }, true, prompt, options);
    }

    /**
     * Returns the display for the given {@link SettingsOption}.
     * 
     * @param option The {@link SettingsOption} to get the display of.
     * @return The display for the given {@link SettingsOption}.
     */
    private static String getSettingDisplay(SettingsOption option) {
        if (option instanceof TimezoneOption) {
            if (option.currentOption().equals("-05:00")) {
                return "EST";
            } else if (option.currentOption().equals("-10:00")) {
                return "HST";  
            } else if (option.currentOption().equals("-07:00")) {
                return "MST";  
            } else {
                return option.currentOption();
            }
        } else {
            return option.currentOption();
        }
    }

}
