package com.georgster.settings;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.georgster.settings.UserSettings.SettingsOption;

public final class TimezoneOption extends UserSettings.SettingsOption {

    private static Set<String> availableOptions = new HashSet<>(ZoneId.SHORT_IDS.keySet());
    private static boolean initialized = false;

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

    @Override
    public String name() {
        return "timezone";
    }

    public Set<String> availableOptions() {
        return availableOptions;
    }
    
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
}
