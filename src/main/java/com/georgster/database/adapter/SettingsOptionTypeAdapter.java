package com.georgster.database.adapter;

import java.io.IOException;

import com.georgster.settings.DefaultColorOption;
import com.georgster.settings.ErrorColorOption;
import com.georgster.settings.InfoColorOption;
import com.georgster.settings.TimezoneOption;
import com.georgster.settings.UserSettings.SettingsOption;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A {@link TypeAdapter} for {@link SettingsOption SettingsOptions}.
 */
public final class SettingsOptionTypeAdapter extends TypeAdapter<SettingsOption> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, SettingsOption value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
    
        // Write type information and object data
        out.beginObject();
        out.name("type").value(value.getClass().getName()); // Storing the class name as type information
        out.name("option").value(value.currentOption()); // Assuming a getter method for option
        out.endObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SettingsOption read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
    
        String type = null;
        String option = null;
    
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("type")) {
                type = in.nextString();
            } else if (name.equals("option")) {
                option = in.nextString();
            } else {
                in.skipValue();
            }
        }
        in.endObject();
    
        if (type == null) {
            throw new JsonParseException("Type information missing in JSON");
        }
    
        // Instantiate the appropriate class based on the type information
        if (type.equals(TimezoneOption.class.getName())) {
            return new TimezoneOption(option);
        } else if (type.equals(DefaultColorOption.class.getName())) {
            return new DefaultColorOption(option);
        } else if (type.equals(ErrorColorOption.class.getName())) {
            return new ErrorColorOption(option);
        } else if (type.equals(InfoColorOption.class.getName())) {
            return new InfoColorOption(option);
        }
    
        // You can add more else if blocks here for other concrete classes
    
        throw new JsonParseException("Unknown type: " + type);
    }
}
