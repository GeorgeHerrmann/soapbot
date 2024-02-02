package com.georgster.settings;

import java.util.HashSet;
import java.util.Set;

import com.georgster.control.manager.Manageable;

/**
 * A {@link Manageable} representing the settings of a user for SOAP Bot.
 * <p>
 * This {@link Manageable Manageable's} identifier is the user id.
 */
public final class UserSettings implements Manageable {

    private final String id; // Identified by user id
    private final Set<SettingsOption> settings;
    
    /**
     * A specific setting for the user.
     */
    public abstract static class SettingsOption {
        protected String option; // the current option for this setting

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

    /**
     * Creates a new {@link UserSettings} with the given id and settings.
     * 
     * @param id      The id of the user.
     * @param settings The settings of the user.    
     */
    public UserSettings(String id, Set<SettingsOption> settings) {
        this.id = id;
        this.settings = settings;
        loadSettings();
    }

    /**
     * Creates a new {@link UserSettings} with the given id and default settings.
     * 
     * @param id The id of the user.
     */
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

    /**
     * Returns the setting of the given class.
     * 
     * @param settingClass The class of the setting to return.
     * @return the setting of the given class.
     */
    public SettingsOption getSetting(Class<? extends SettingsOption> settingClass) {
        return settings.stream().filter(settingClass::isInstance).findFirst().orElseThrow();
    }

    /**
     * Returns the setting of the given name.
     * 
     * @param settingName The name of the setting to return.
     * @return the setting of the given name.
     */
    public SettingsOption getSetting(String settingName) {
        return settings.stream().filter(setting -> setting.name().equals(settingName)).findFirst().orElseThrow();
    }

    /**
     * Returns all settings for the user.
     * 
     * @return all settings for the user.
     */
    public Set<SettingsOption> getAllSettings() {
        return settings;
    }

    /**
     * Returns the timezone setting for the user.
     * 
     * @return the timezone setting for the user.
     */
    public SettingsOption getTimezoneSetting() {
        return getSetting(TimezoneOption.class);
    }

    /**
     * Returns the default color setting for the user.
     * 
     * @return the default color setting for the user.
     */
    public SettingsOption getDefaultColorSetting() {
        return getSetting(DefaultColorOption.class);
    }

    /**
     * Returns the error color setting for the user.
     * 
     * @return the error color setting for the user.
     */
    public SettingsOption getErrorColorSetting() {
        return getSetting(ErrorColorOption.class);
    }

    /**
     * Returns the info color setting for the user.
     * 
     * @return the info color setting for the user.
     */
    public SettingsOption getInfoColorSetting() {
        return getSetting(InfoColorOption.class);
    }

    /**
     * Loads the settings for the user.
     */
    public void loadSettings() {
        // add guard clauses to check if each setting is already loaded (identified by class type, TimezoneOption.class, DefaultColorOption.class)

        if (!settings.stream().anyMatch(setting -> setting instanceof TimezoneOption)) {
            this.settings.add(new TimezoneOption());
        }

        if (!settings.stream().anyMatch(setting -> setting instanceof DefaultColorOption)) {
            this.settings.add(new DefaultColorOption());
        }

        if (!settings.stream().anyMatch(setting -> setting instanceof ErrorColorOption)) {
            this.settings.add(new ErrorColorOption());
        }

        if (!settings.stream().anyMatch(setting -> setting instanceof InfoColorOption)) {
            this.settings.add(new InfoColorOption());
        }
    }

}
