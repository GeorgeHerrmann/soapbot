package com.georgster.api.faceit.exception;

/**
 * Custom exception for Faceit API-related errors.
 * <p>
 * This exception is thrown when there are issues communicating with the Faceit API,
 * including network errors, invalid responses, rate limiting, or server errors.
 * <p>
 * Supports standardized error messages per FR-011 requirement.
 */
public class FaceitAPIException extends Exception {
    
    /**
     * Creates a new FaceitAPIException with the specified error message.
     * 
     * @param message The error message describing the exception
     */
    public FaceitAPIException(String message) {
        super(message);
    }
    
    /**
     * Creates a new FaceitAPIException with the specified error message and cause.
     * 
     * @param message The error message describing the exception
     * @param cause The underlying cause of the exception
     */
    public FaceitAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
