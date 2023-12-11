package com.georgster.settings;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public final class TimezoneOption extends UserSettings.SettingsOption {

    private static Set<String> availableOptions = new HashSet<>(ZoneId.SHORT_IDS.keySet());

    public TimezoneOption() {
        super("-05:00");
        if (availableOptions == null) {
            availableOptions.remove("VST");
            availableOptions.remove("AGT");
            availableOptions.remove("IST");
        }
    }

    public TimezoneOption(String option) {
        super(option);
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
}
