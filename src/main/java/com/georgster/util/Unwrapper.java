package com.georgster.util;

/**
 * Wrapper class to retrieve a non-null object from a lambda.
 */
public class Unwrapper<T> {
    private T object;

    /**
     * Returns the object.
     * 
     * @return the object.
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the object.
     * 
     * @param object the object.
     */
    public void setObject(T object) {
        this.object = object;
    }
}
