package com.georgster.profile.adapter;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public abstract class DatabaseObjectDeserializer<T> implements JsonDeserializer<T> {

    private T object;
    private String[] uniqueIdentifiers;

    protected DatabaseObjectDeserializer(T object) {
        this.object = object;
    }

    protected DatabaseObjectDeserializer(String... uniqueIdentifiers) {
        this.uniqueIdentifiers = uniqueIdentifiers;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        Type type = getType();
        JsonElement element = gson.toJsonTree(object, type);

        return context.deserialize(element, type);
    }

    public abstract Type getType();

    public Type getType(String json) throws JsonParseException {
        for (String identifier : uniqueIdentifiers) {
            if (json.contains(identifier)) {
                return getType();
            }
        }
        throw new JsonParseException("Could not find a matching type for the given json");
    }
}
