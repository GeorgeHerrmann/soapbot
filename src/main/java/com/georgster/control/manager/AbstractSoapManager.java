package com.georgster.control.manager;

import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.database.adapter.DatabaseObjectClassAdapter;

import discord4j.core.object.entity.Guild;

/**
 * A {@link SoapManager} which can manage extending or implementing objects that are stored in SOAPBot's database
 * in the form of their parent class. A valid {@link DatabaseObjectClassAdapter} must be provided to convert the
 * objects from the database to their appropriate type.
 */
public abstract class AbstractSoapManager<T extends Manageable> extends SoapManager<T> {
    protected DatabaseObjectClassAdapter<T> adapter; // The adapter that will be used to convert the objects from the database.

    /**
     * Creates a new {@link AbstractSoapManager} which will access the database using the given paramaters.
     * 
     * @param context The context of the SOAPClient that is using this manager.
     * @param profileType The type of profile that this manager will be accessing.
     * @param observeeClass The class of the objects that this manager will be managing.
     * @param identifierName The name of the identifier field in the database.
     * @param adapter The adapter that will be used to convert the objects from the database.
     */
    protected AbstractSoapManager(ClientContext context, ProfileType profileType, Class<T> observeeClass, String identifierName, DatabaseObjectClassAdapter<T> adapter) {
        super(context, profileType, observeeClass, identifierName);
        this.adapter = adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        dbService.getAllObjects(adapter).forEach(this::add);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(T observee) {
        if (!exists(observee.getIdentifier())) {
            dbService.addObjectIfNotExists(observee, identifierName, observee.getIdentifier(), adapter);
            observees.add(observee);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(T observee) {
        return observees.contains(observee);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String identifier) {
        return observees.stream().anyMatch(observee -> observee.getIdentifier().equals(identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(T observee) {
        if (exists(observee)) {
            dbService.removeObjectIfExists(identifierName, observee.getIdentifier(), adapter);
            observees.remove(observee);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String identifier) {
        observees.stream().filter(observee -> observee.getIdentifier().equals(identifier)).forEach(observee -> {
            dbService.removeObjectIfExists(identifierName, identifier, adapter);
            observees.remove(observee);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll() {
        observees.forEach(this::remove);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(String identifier) {
        return observees.stream().filter(observee -> observee.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAll() {
        return observees;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(T observee) {
        observees.stream().filter(examiner -> examiner.getIdentifier().equals(observee.getIdentifier())).forEach(examiner -> {
            observees.set(observees.indexOf(examiner), observee); //Replace the old observee with the new one
            dbService.updateObjectIfExists(observee, identifierName, observee.getIdentifier(), adapter);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return observees.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return observees.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Guild getGuild() {
        return handler.getGuild();
    }
}
