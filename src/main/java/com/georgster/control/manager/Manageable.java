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

    /**
     * Attempts to deserialize this {@link Manageable} into a JSON String.
     * 
     * @return The deserialized JSON String representation of this {@link Manageable}.
     */
    default String deserialize() {
        return GSON.toJson(this);
    }

    /**
     * Attempts to serialize a JSON String into a {@link Manageable} of the given {@code classType}.
     * 
     * @param <T> The type of the resulting {@link Manageable} based on the given {@link Class} type.
     * @param json The JSON String.
     * @param classType The {@link Class} type.
     * @return The serialized {@link Manageable}.
     */
    public static <T extends Manageable> T serialize(String json, Class<T> classType) {
        return GSON.fromJson(json, classType);
    }
}
