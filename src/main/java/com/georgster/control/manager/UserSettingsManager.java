package com.georgster.control.manager;

import com.georgster.database.ProfileType;
import com.georgster.settings.UserSettings;

public final class UserSettingsManager extends GlobalSoapManager<UserSettings> {

    public UserSettingsManager() {
        super(ProfileType.SETTINGS, UserSettings.class, "id");
    }

    /**
     * {@inheritDoc}
     */
    /*@Override
    public void load() {
        dbService.getAllObjects().forEach(settings -> {
            settings.loadSettings();
            add(settings);
            update(settings);
        });
    }*/
    
}
