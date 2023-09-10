package com.georgster.control.manager;

import com.google.gson.Gson;

/**
 * An interface for objects that can be managed by a {@link SoapManager} or {@link AbstractSoapManager}.
 * All Manageable objects must have a unique identifier that can be retrieved by {@link #getIdentifier()}.
 */
public interface Manageable {
    /** Gson responsible for serialization and deserialization of {@link Manageable Manageables} */
    static final Gson GSON = new Gson();

    /**
     * Returns the identifier of this object.
     * 
     * @return the identifier
     */
    public String getIdentifier();
}
