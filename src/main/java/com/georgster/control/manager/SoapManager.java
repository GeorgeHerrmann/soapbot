package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.DatabaseService;
import com.georgster.database.ProfileType;
import com.georgster.util.GuildInteractionHandler;

import discord4j.core.object.entity.Guild;

/**
 * A framework for managing non-extending nor implementing objects that are stored in SOAPBot's database.
 */
public abstract class SoapManager<T extends Manageable> {
    protected String identifierName; // The name of the identifier field in the database.
    protected List<T> observees; // The objects that this manager is managing.
    protected DatabaseService<T> dbService; // The service that this manager will use to access the database.
    protected GuildInteractionHandler handler; // The handler that this manager will use to interact with the guild.

    /**
     * Creates a new SoapManager which will access the database using the given paramaters.
     * 
     * @param context The context of the SOAPClient that is using this manager.
     * @param profileType The type of profile that this manager will be accessing.
     * @param observeeClass The class of the objects that this manager will be managing.
     * @param identifierName The name of the identifier field in the database.
     */
    protected SoapManager(ClientContext context, ProfileType profileType, Class<T> observeeClass, String identifierName) {
        this.handler = new GuildInteractionHandler(context.getGuild());
        this.dbService = new DatabaseService<>(handler.getId(), profileType, observeeClass);
        this.observees = new ArrayList<>();
        this.identifierName = identifierName;
    }

    /**
     * Adds an object to the manager.
     * 
     * @param observee The object to add.
     */
    public void add(T observee) {
        if (!exists(observee.getIdentifier())) {
            dbService.addObjectIfNotExists(observee, identifierName, observee.getIdentifier());
            observees.add(observee);
        }
    }

    /**
     * Checks if an object exists in the manager.
     * 
     * @param observee The object to check.
     * @return True if the object exists in the manager, false otherwise.
     */
    public boolean exists(T observee) {
        return observees.contains(observee);
    }

    /**
     * Checks if an object exists in the manager.
     * 
     * @param identifier The identifier of the object to check.
     * @return True if the object exists in the manager, false otherwise.
     */
    public boolean exists(String identifier) {
        return observees.stream().anyMatch(observee -> observee.getIdentifier().equals(identifier));
    }

    /**
     * Removes an object from the manager.
     * 
     * @param observee The object to remove.
     */
    public void remove(T observee) {
        if (exists(observee)) {
            dbService.removeObjectIfExists(identifierName, observee.getIdentifier());
            observees.remove(observee);
        }
    }

    /**
     * Removes an object from the manager.
     * 
     * @param identifier The identifier of the object to remove.
     */
    public void remove(String identifier) {
        observees.stream().filter(observee -> observee.getIdentifier().equals(identifier)).forEach(observee -> {
            dbService.removeObjectIfExists(identifierName, identifier);
            observees.remove(observee);
        });
    }

    /**
     * Removes all objects from the manager.
     */
    public void removeAll() {
        observees.forEach(observee -> dbService.removeObjectIfExists(identifierName, observee.getIdentifier()));
        observees.clear();
    }

    /**
     * Gets an object from the manager.
     * 
     * @param identifier The identifier of the object to get.
     * @return The object with the given identifier, or null if no such object exists.
     */
    public T get(String identifier) {
        return observees.stream().filter(observee -> observee.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    /**
     * Gets all objects from the manager.
     * 
     * @return A list of all objects in the manager.
     */
    public List<T> getAll() {
        return observees;
    }

    /**
     * Updates an object in the manager.
     * 
     * @param observee The object to update.
     */
    public void update(T observee) {
        observees.stream().filter(examiner -> examiner.getIdentifier().equals(observee.getIdentifier())).forEach(examiner -> {
            observees.set(observees.indexOf(examiner), observee); //Replace the old observee with the new one
            dbService.updateObjectIfExists(observee, identifierName, observee.getIdentifier());
        });
    }

    /**
     * Gets the number of objects in the manager.
     * 
     * @return The number of objects in the manager.
     */
    public int getCount() {
        return observees.size();
    }

    /**
     * Checks if the manager is empty.
     * 
     * @return True if the manager is empty, false otherwise.
     */
    public boolean isEmpty() {
        return observees.isEmpty();
    }

    /**
     * Gets the guild that this manager is managing objects for.
     * 
     * @return The guild that this manager is managing objects for.
     */
    
    public Guild getGuild() {
        return handler.getGuild();
    }
}
