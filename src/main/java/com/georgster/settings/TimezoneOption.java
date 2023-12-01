package com.georgster.settings;

import java.time.ZoneId;
import java.util.Set;

public final class TimezoneOption extends UserSettings.SettingsOption {

    public TimezoneOption() {
        super("US/Eastern");
    }

    public TimezoneOption(String option) {
        super(option);
    }

    @Override
    public String name() {
        return "timezone";
    }

    public Set<String> availableOptions() {
        return ZoneId.getAvailableZoneIds();
    }

}
