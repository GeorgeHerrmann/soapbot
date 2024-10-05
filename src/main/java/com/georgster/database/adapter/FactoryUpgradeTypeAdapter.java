package com.georgster.database.adapter;

import java.io.IOException;

import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;
import com.georgster.coinfarm.model.upgrades.FactoryUpgradeTracks;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A {@link TypeAdapter} for {@link FactoryUpgrade FactoryUpgrades}.
 */
public final class FactoryUpgradeTypeAdapter extends TypeAdapter<FactoryUpgrade> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, FactoryUpgrade value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
    
        // Write type information and object data
        out.beginObject();
        out.name("upgrade_name").value(value.getName());
        out.name("reward_track_name").value(value.getTrackName());
        out.endObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FactoryUpgrade read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
    
        String upgradeName = null;
        String trackName = null;
    
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("upgrade_name")) {
                upgradeName = in.nextString();
            } else if (name.equals("reward_track_name")) {
                trackName = in.nextString();
            } else {
                in.skipValue();
            }
        }
        in.endObject();
    
        if (upgradeName == null) {
            throw new JsonParseException("Upgrade Name information for a FactoryUpgrade missing in JSON. Conversion failed.");
        } else if (trackName == null) {
            throw new JsonParseException("Track Name information for a FactoryUpgrade missing in JSON. Conversion failed.");
        }

        if (upgradeName.equals("No Upgrade")) {
            throw new JsonParseException("An AbsentFactoryUpgrade should not be stored in a Member's CoinFactory and cannot be created from JSON. Conversion failed.");
        }

        try {
            return FactoryUpgradeTracks.getUpgrade(trackName, upgradeName);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Error while converting JSON to FactoryUpgrade: " + e.getMessage(), e);
        }
    }
}
