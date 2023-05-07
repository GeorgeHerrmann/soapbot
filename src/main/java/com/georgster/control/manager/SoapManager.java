package com.georgster.control.manager;

import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.profile.DatabaseService;
import com.georgster.profile.ProfileType;
import com.georgster.util.GuildInteractionHandler;

public abstract class SoapManager<T> {
    List<T> observees;
    DatabaseService<T> dbService;
    GuildInteractionHandler handler;

    protected SoapManager(ClientContext context, ProfileType profileType, Class<T> observeeClass) {
        this.handler = new GuildInteractionHandler(context.getGuild());
        this.dbService = new DatabaseService<>(handler.getId(), profileType, observeeClass);
        this.observees = dbService.getAllObjects();
    }
}
