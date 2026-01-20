package com.georgster.api.faceit.exception;

/**
 * Exception thrown when a Faceit player cannot be found through any lookup method.
 * <p>
 * This exception extends {@link FaceitAPIException} and is used specifically for
 * player lookup failures, whether by Discord mention, Faceit username, or Steam ID.
 * <p>
 * Provides user-friendly error messages indicating which lookup method failed.
 */
public class PlayerNotFoundException extends FaceitAPIException {
    
    /**
     * Creates a new PlayerNotFoundException with the specified error message.
     * 
     * @param message The error message describing which lookup method failed
     */
    public PlayerNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Creates a new PlayerNotFoundException with the specified error message and cause.
     * 
     * @param message The error message describing which lookup method failed
     * @param cause The underlying cause of the exception
     */
    public PlayerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
