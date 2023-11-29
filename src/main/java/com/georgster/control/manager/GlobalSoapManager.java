package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.database.DatabaseService;
import com.georgster.database.ProfileType;

/**
 * A {@link SoapManager} which manages non-guild-specific, non-extending nor implementing (global) {@link Manageable Manageables} that are stored in SOAPBot's database.
 * <p>
 * A {@link GlobalSoapManager} will communicate with the {@code GLOBAL} database profile, rather than a guild-specific one like a {@link GuildedSoapManager}.
 * 
 * @see {@link AbstractGlobalSoapManager} for {@link SoapManager} that can manage extending or implementing objects.
 * @see {@link GuildedSoapManager} for {@link SoapManager} that manages guild-specific objects.
 */
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

    /**
     * {@inheritDoc}
     */
    public void load() {
        dbService.getAllObjects().forEach(this::add);
    }

    /**
     * {@inheritDoc}
     */
    public void add(T observee) {
        if (!exists(observee.getIdentifier())) {
            dbService.addObjectIfNotExists(observee, identifierName, observee.getIdentifier());
            observees.add(observee);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(T observee) {
        return observees.contains(observee);
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(String identifier) {
        return observees.stream().anyMatch(observee -> observee.getIdentifier().equals(identifier));
    }

    /**
     * {@inheritDoc}
     */
    public void remove(T observee) {
        if (exists(observee)) {
            dbService.removeObjectIfExists(identifierName, observee.getIdentifier());
            observees.remove(observee);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String identifier) {
        observees.stream().filter(observee -> observee.getIdentifier().equals(identifier)).forEach(observee -> {
            dbService.removeObjectIfExists(identifierName, identifier);
            observees.remove(observee);
        });
    }

    /**
     * {@inheritDoc}
     */
    public void removeAll() {
        observees.forEach(observee -> dbService.removeObjectIfExists(identifierName, observee.getIdentifier()));
        observees.clear();
    }

    /**
     * {@inheritDoc}
     */
    public T get(String identifier) {
        return observees.stream().filter(observee -> observee.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    public List<T> getAll() {
        return observees;
    }

    /**
     * {@inheritDoc}
     */
    public void update(T observee) {
        observees.stream().filter(examiner -> examiner.getIdentifier().equals(observee.getIdentifier())).forEach(examiner -> {
            observees.set(observees.indexOf(examiner), observee); //Replace the old observee with the new one
            dbService.updateObjectIfExists(observee, identifierName, observee.getIdentifier());
        });
    }

    /**
     * {@inheritDoc}
     */
    public void update(String identifier, T observee) {
        observees.stream().filter(examiner -> examiner.getIdentifier().equals(identifier)).forEach(examiner -> {
            observees.set(observees.indexOf(examiner), observee); //Replace the old observee with the new one
            dbService.updateObjectIfExists(observee, identifierName, identifier);
        });
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {
        return observees.size();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return observees.isEmpty();
    }
}
