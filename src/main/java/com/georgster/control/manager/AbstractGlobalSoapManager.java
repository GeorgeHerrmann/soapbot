package com.georgster.control.manager;

import com.georgster.database.ProfileType;
import com.georgster.database.adapter.DatabaseObjectClassAdapter;

/**
 * A {@link GlobalSoapManager} which manages non-guild-specific, extending or implementing (global) {@link Manageable Manageables} that are stored in SOAPBot's database.
 * <p>
 * An {@link AbstractGlobalSoapManager} will communicate with the {@code GLOBAL} database profile, rather than a guild-specific one like a {@link GuildedSoapManager}.
 * 
 * @see {@link GlobalSoapManager} for {@link SoapManager} that can manage non-extending nor implementing objects.
 * @see {@link GuildedSoapManager} for {@link SoapManager} that manages guild-specific objects.
 */
public abstract class AbstractGlobalSoapManager<T extends Manageable> extends GlobalSoapManager<T> {
    
    protected DatabaseObjectClassAdapter<T> adapter; // The adapter that will be used to convert the objects from the database.

    /**
     * Creates a new {@link AbstractGlobalSoapManager} which will access the database using the given paramaters.
     * 
     * @param profileType The type of profile that this manager will be accessing.
     * @param observeeClass The class of the objects that this manager will be managing.
     * @param identifierName The name of the identifier field in the database.
     * @param adapter The adapter that will be used to convert the objects from the database.
     */
    protected AbstractGlobalSoapManager(ProfileType profileType, Class<T> observeeClass, String identifierName, DatabaseObjectClassAdapter<T> adapter) {
        super(profileType, observeeClass, identifierName);
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
    public void update(T observee) {
        observees.stream().filter(examiner -> examiner.getIdentifier().equals(observee.getIdentifier())).forEach(examiner -> {
            observees.set(observees.indexOf(examiner), observee); //Replace the old observee with the new one
            dbService.updateObjectIfExists(observee, identifierName, observee.getIdentifier(), adapter);
        });
    }
}
