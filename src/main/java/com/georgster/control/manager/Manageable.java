package com.georgster.control.manager;

import java.lang.reflect.Type;

import com.georgster.database.adapter.DatabaseObjectClassAdapter;
import com.georgster.database.adapter.SettingsOptionTypeAdapter;
import com.georgster.settings.UserSettings.SettingsOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * An object that can be managed by a {@link GuildedSoapManager} or {@link GlobalSoapManager}.
 * <p>
 * If the {@link Managable} is an abstract class, it should be managed by a {@link AbstractGuildedSoapManager} or
 * {@link AbstractGlobalSoapManager}, and use a {@link DatabaseObjectClassAdapter} to serialize and deserialize the
 * object.
 * <p>
 * If the {@link Manageble} contains objects that are abstract (without final class Type declaration), a {@link com.google.gson.TypeAdapter TypeAdapter}
 * should be used to serialize and deserialize the object and registered with the {@link #GSON} instance.
 * <p>
 * All Manageable objects must have a unique identifier that can be retrieved by {@link #getIdentifier()}.
 */
public interface Manageable {
    /** Gson responsible for serialization and deserialization of {@link Manageable Manageables} */
    static final Gson GSON = new GsonBuilder().registerTypeAdapter(SettingsOption.class, new SettingsOptionTypeAdapter()).create();

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

    /**
     * Attempts to serialize a JSON String into a {@link Manageable} of the given {@code type}.
     * 
     * @param <T> The type of the resulting {@link Manageable} based on the given {@link Type}.
     * @param json The JSON String.
     * @param type The {@link Type}.
     * @return The serialized {@link Manageable}.
     * @throws JsonParseException If the JSON String cannot be automatically serialized.
     */
    public static <T extends Manageable> T serialize(String json, Type type) throws JsonParseException {
        return GSON.fromJson(json, type);
    }

    /**
     * Returns the default {@link Gson} instance used for serialization and deserialization of {@link Manageable Manageables}.
     * 
     * @return the default {@link Gson} instance.
     */
    public static Gson getGson() {
        return GSON;
    }
}
