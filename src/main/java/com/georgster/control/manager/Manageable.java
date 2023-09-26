package com.georgster.control.manager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * An object that can be managed by a {@link SoapManager} or {@link AbstractSoapManager}.
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
     * Attempts to deserialize this {@link Manageable} into a JSON String using the default {@link Class} type.
     * Generally used when a {@link Manageable} is not generic. If the {@link Manageable} is generic, use
     * {@link #deserialize(Class)}.
     * 
     * @return The deserialized JSON String representation of this {@link Manageable}.
     * @throws JsonParseException If the {@link Manageable} cannot be automatically deserialized.
     * @see {@link #deserialize(Class)} for generic {@link Manageable Manageables}.
     */
    default String deserialize() throws JsonParseException {
        return GSON.toJson(this);
    }

    /**
     * Attempts to deserialize this {@link Manageable} into a JSON String of the given {@code classType}.
     * Generally used when a {@link Manageable} is generic and cannot be automatically deserialized by
     * {@link #deserialize()}.
     * 
     * @param classType The {@link Class} type of the {@link Manageable}.
     * @return The deserialized JSON String representation of this {@link Manageable}.
     * @throws JsonParseException If the {@link Manageable} cannot be automatically deserialized.
     * @see {@link #deserialize()} for non-generic {@link Manageable Manageables}.
     */
    default String deserialize(Class<? extends Manageable> classType) throws JsonParseException {
        return GSON.toJson(this, classType);
    }

    /**
     * Attempts to serialize a JSON String into a {@link Manageable} of the given {@code classType}.
     * 
     * @param <T> The type of the resulting {@link Manageable} based on the given {@link Class} type.
     * @param json The JSON String.
     * @param classType The {@link Class} type.
     * @return The serialized {@link Manageable}.
     * @throws JsonParseException If the JSON String cannot be automatically serialized.
     */
    public static <T extends Manageable> T serialize(String json, Class<T> classType) throws JsonParseException {
        return GSON.fromJson(json, classType);
    }
}
