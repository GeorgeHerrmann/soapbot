package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.DatabaseService;
import com.georgster.database.ProfileType;
import com.georgster.util.handler.GuildInteractionHandler;

public abstract class GlobalSoapManager<T extends Manageable> implements SoapManager<T> {
    protected String identifierName; // The name of the identifier field in the database.
    protected List<T> observees; // The objects that this manager is managing.
    protected DatabaseService<T> dbService; // The service that this manager will use to access the database.

    /**
     * Creates a new {@link GlobalSoapManager} which will access the database using the given paramaters.
     * 
     * @param context The context of the SOAPClient that is using this manager.
     * @param profileType The type of profile that this manager will be accessing.
     * @param observeeClass The class of the objects that this manager will be managing.
     * @param identifierName The name of the identifier field in the database.
     */
    protected GlobalSoapManager(ProfileType profileType, Class<T> observeeClass, String identifierName) {
        this.dbService = new DatabaseService<>("GLOBAL", profileType, observeeClass);
        this.observees = new ArrayList<>();
        this.identifierName = identifierName;
    }
}
