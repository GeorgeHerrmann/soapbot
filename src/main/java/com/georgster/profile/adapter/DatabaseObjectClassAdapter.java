package com.georgster.profile.adapter;

import com.google.gson.JsonParseException;

/**
 * A facade for an adapter that allows for the deserialization of inherited
 * {@code DatabaseObject}s without knowing their type beyond the
 * abstract class or interface they implement.
 */
public abstract class DatabaseObjectClassAdapter<T> {

    private String[] uniqueIdentifiers; //The unique identifiers for each type of object

    /**
     * Constructs a {@code DatabaseObjectClassAdapter} with the given
     * unique identifiers for each type of object.
     * 
     * @param uniqueIdentifiers the unique identifiers for each type of object
     */
    protected DatabaseObjectClassAdapter(String... uniqueIdentifiers) {
        this.uniqueIdentifiers = uniqueIdentifiers;
    }

    /**
     * Returns the type of object that matches the given keyword.
     * 
     * @param keyword the keyword to match
     * @return the type of object that matches the given keyword
     * @throws JsonParseException if no object matches the given keyword
     */
    protected abstract Class<? extends T> getType(String keyword) throws JsonParseException;

    /**
     * Returns the type of object that matches the given json.
     * 
     * @param json the json to match
     * @return the type of object that matches the given json
     * @throws JsonParseException if no object matches the given json
     */
    public Class<? extends T> getClass(String json) throws JsonParseException {
        for (String identifier : uniqueIdentifiers) {
            if (json.contains(identifier)) {
                return getType(identifier);
            }
        }
        throw new JsonParseException("Could not find a matching type for the given json");
    }
}
