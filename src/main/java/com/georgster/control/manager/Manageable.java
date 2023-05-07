package com.georgster.control.manager;

/**
 * An interface for objects that can be managed by a {@link SoapManager} or {@link AbstractSoapManager}.
 * All Manageable objects must have a unique identifier that can be retrieved by {@link #getIdentifier()}.
 */
public interface Manageable {
    /**
     * Returns the identifier of this object.
     * 
     * @return the identifier
     */
    public String getIdentifier();
}
