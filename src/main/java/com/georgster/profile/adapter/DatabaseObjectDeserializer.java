package com.georgster.profile.adapter;

import com.google.gson.JsonParseException;

public abstract class DatabaseObjectDeserializer<T> {

    private String[] uniqueIdentifiers;

    protected DatabaseObjectDeserializer(String... uniqueIdentifiers) {
        this.uniqueIdentifiers = uniqueIdentifiers;
    }

    protected abstract Class<? extends T> getType(String keyword) throws JsonParseException;

    public Class<? extends T> getClass(String json) throws JsonParseException {
        for (String identifier : uniqueIdentifiers) {
            if (json.contains(identifier)) {
                return getType(identifier);
            }
        }
        throw new JsonParseException("Could not find a matching type for the given json");
    }
}
