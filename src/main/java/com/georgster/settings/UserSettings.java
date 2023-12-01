package com.georgster.settings;

import java.util.HashSet;
import java.util.Set;

import com.georgster.control.manager.Manageable;

/**
 * A {@link Manageable} representing the settings of a user for SOAP Bot.
 */
public final class UserSettings implements Manageable {

    private final String id; // Identified by user id
    private final Set<SettingsOption> settings;
    
    /**
     * A specific setting for the user.
     */
    public abstract static class SettingsOption {
        private String option; // the current option for this setting

        /**
         * Creates a new setting with the given default option.
         * 
         * @param defaultOption the default option for this setting.
         * @throws IllegalArgumentException if the default option is not available.
         */
        protected SettingsOption(String defaultOption) throws IllegalArgumentException {
            setOption(defaultOption);
        }

        /**
         * Returns the name of this setting.
         * 
         * @return the name of this setting.
         */
        public abstract String name();

        /**
         * Returns a set of available options for this setting.
         * 
         * @return a set of available options for this setting.
         */
        public abstract Set<String> availableOptions();

        /**
         * Returns the current option for this setting.
         * 
         * @return the current option for this setting.
         */
        public String currentOption() {
            return option;
        }

        /**
         * Sets the option for this setting. If the option is not available, an
         * IllegalArgumentException is thrown.
         * 
         * @param option
         * @throws IllegalArgumentException
         */
        public void setOption(String option) throws IllegalArgumentException {
            if (availableOptions().contains(option)) {
                this.option = option;
            } else {
                throw new IllegalArgumentException("Option " + option + " is not available for " + name() + ". Available options are: " + availableOptions().toString());
            }
        }
    }

    public UserSettings(String id, Set<SettingsOption> settings) {
        this.id = id;
        this.settings = settings;
    }

    public UserSettings(String id) {
        this.id = id;
        this.settings = new HashSet<>();
        loadSettings();
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return id;
    }

    public SettingsOption getSetting(Class<? extends SettingsOption> settingClass) {
        return settings.stream().filter(settingClass::isInstance).findFirst().orElse(null);
    }

    public SettingsOption getTimezoneSetting() {
        return getSetting(TimezoneOption.class);
    }

    private void loadSettings() {
        this.settings.add(new TimezoneOption());
    }

}
