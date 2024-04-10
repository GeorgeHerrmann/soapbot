package com.georgster.settings;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.georgster.settings.UserSettings.SettingsOption;

/**
 * A {@link SettingsOption} that represents a timezone. The option is stored as a String
 * that is a valid timezone ID. The available options are all the timezone IDs
 * that are valid for the system. The default option is the timezone of the
 * system.
 * 
 * @see java.time.ZoneId
 * @see com.georgster.settings.UserSettings.SettingsOption
 */
public final class TimezoneOption extends UserSettings.SettingsOption {

    private static Set<String> availableOptions = new HashSet<>(ZoneId.SHORT_IDS.keySet());
    private static boolean initialized = false;

    /**
     * Creates a new {@link TimezoneOption} with the default option being the
     * timezone of the system.
     */
    public TimezoneOption() {
        super("-05:00");
        if (!initialized) { // Though technically the map would be the same calling this multiple times, it's more efficient to do it once
            availableOptions.remove("VST");
            availableOptions.remove("AGT");
            availableOptions.remove("IST");
            availableOptions.remove("NET");
            initialized = true;
        }
    }

    /**
     * Creates a new {@link TimezoneOption} with the given option.
     * 
     * @param option The option to set the {@link TimezoneOption} to.
     */
    public TimezoneOption(String option) {
        super(option);
        if (!initialized) { // Though technically the map would be the same calling this multiple times, it's more efficient to do it once
            availableOptions.remove("VST");
            availableOptions.remove("AGT");
            availableOptions.remove("IST");
            availableOptions.remove("NET");
            initialized = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "timezone";
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> availableOptions() {
        return availableOptions;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setOption(String option) throws IllegalArgumentException {
        if (option.length() == 3) { // if it's a timezone abbreviation
            option = option.toUpperCase();
        }
        if (availableOptions().contains(option)) {
            this.option = ZoneId.SHORT_IDS.get(option);
        } else if (ZoneId.SHORT_IDS.containsValue(option)) {
            this.option = option;
        } else {
            throw new IllegalArgumentException("Invalid timezone: " + option);
        }
    }

    /**
     * Returns the display of the given {@link SettingsOption}. If the option is a
     * {@link TimezoneOption}, then the display is the timezone abbreviation. If the
     * option is not a {@link TimezoneOption}, then an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param option The {@link SettingsOption} to get the display of.
     * @return The display of the given {@link SettingsOption}.
     */
    public static String getSettingDisplay(SettingsOption option) {
        if (option instanceof TimezoneOption) {
            if (option.currentOption().equals("-05:00")) {
                return "EST";
            } else if (option.currentOption().equals("-10:00")) {
                return "HST";  
            } else if (option.currentOption().equals("-07:00")) {
                return "MST";  
            } else {
                return ZoneId.SHORT_IDS.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey)).get(option.currentOption()); // Flips the map and gets the key from the value
            }
        } else {
            throw new IllegalArgumentException("Invalid SettingsOption: " + option.name() + " is not a TimezoneOption");
        }
    }

    /**
     * Returns the Java timezone ID of the given {@link SettingsOption}. If the
     * option is a {@link TimezoneOption}, then the Java timezone ID is returned. If
     * the option is not a {@link TimezoneOption}, then an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @param option The {@link SettingsOption} to get the Java timezone ID of.
     * @return The Java timezone ID of the given {@link SettingsOption}.
     */
    public static String getJavaTimeString(SettingsOption option) {
        if (option instanceof TimezoneOption) {
            if (option.currentOption().equals("-05:00")) {
                return "America/New_York";
            } else {
                return option.currentOption(); // Flips the map and gets the key from the value
            }
        } else {
            throw new IllegalArgumentException("Invalid SettingsOption: " + option.name() + " is not a TimezoneOption");
        }
    }
}
