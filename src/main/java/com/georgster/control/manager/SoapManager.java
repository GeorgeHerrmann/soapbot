package com.georgster.control.manager;

import java.util.List;

/**
 * A manager which manages {@link Manageable Manageables} that are stored in SOAPBot's database.
 */
public interface SoapManager<T extends Manageable> {

    /**
     * Wipes the local cache and loads all objects from the database into this manager.
     */
    public void load();

    /**
     * Adds an object to the manager and the database, if an object with the same identifier does not already exist.
     * 
     * @param observee The object to add.
     */
    public void add(T observee);

    /**
     * Checks if an object with the same identifier as the {@code observee} exists in the manager.
     * 
     * @param observee The object to check.
     * @return {@code true} if the object exists in the manager, {@code false} otherwise.
     */
    public boolean exists(T observee);

    /**
     * Checks if an object with the given identifier exists in the manager.
     * 
     * @param identifier The identifier of the object to check.
     * @return {@code true} if the object exists in the manager, {@code false} otherwise.
     */
    public boolean exists(String identifier);

    /**
     * Removes an object with the same identifier as the {@code observee} from the manager and the database.
     * 
     * @param observee The object to remove.
     */
    public void remove(T observee);

    /**
     * Removes an object with the given identifier from the manager and the database.
     * 
     * @param identifier The identifier of the object to remove.
     */
    public void remove(String identifier);

    /**
     * Removes all objects from the manager and the database.
     */
    public void removeAll();

    /**
     * Updates an object with the same identifier as the {@code observee} in the manager and the database with {@code observee}.
     * 
     * @param observee The object to update.
     */
    public void update(T observee);

    /**
     * Updates an object with the given identifier in the manager and the database with {@code observee}.
     * 
     * @param identifier The identifier of the object to update.
     * @param observee The object to update.
     */
    public void update(String identifier, T observee);

    /**
     * Returns the number of objects in this manager.
     * 
     * @return the number of objects in this manager.
     */
    public int getCount();

    /**
     * Returns the object with the given identifier, or {@code null} if none exists.
     * 
     * @param identifier the identifier of the object to get.
     * @return the object with the given identifier, or {@code null} if none exists.
     */
    public T get(String identifier);

    /**
     * Returns all objects in this manager as a {@link List}.
     * 
     * @return all objects in this manager as a {@link List}.
     */
    public List<T> getAll();

    /**
     * Returns whether or not this manager is empty.
     * 
     * @return {@code true} if this manager is empty, {@code false} otherwise.
     */
    public boolean isEmpty();
}
