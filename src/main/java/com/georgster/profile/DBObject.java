package com.georgster.profile;

/**
 * A Wrapper class representing an object that has been retrieved from the database.
 * Could be a single object, an entire collection as a List, or a single value from a document.
 */
public class DBObject<T> {
    private T object;

    /**
     * Returns the object retrieved from the database.
     * 
     * @return the object retrieved from the database
     */
    protected T getObject() {
        return object;
    }

    /**
     * Sets the object retrieved from the database.
     * 
     * @param object the object retrieved from the database
     */
    protected void setObject(T object) {
        this.object = object;
    }
}
