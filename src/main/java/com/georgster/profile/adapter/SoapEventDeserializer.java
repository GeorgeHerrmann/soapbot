package com.georgster.profile.adapter;

import com.georgster.events.SoapEvent;
import com.georgster.events.reserve.ReserveEvent;
import com.google.gson.JsonParseException;

public class SoapEventDeserializer extends DatabaseObjectDeserializer<SoapEvent> {

    public SoapEventDeserializer() {
        super("reserved");
    }
    
    @Override
    public Class<? extends SoapEvent> getType(String keyword) throws JsonParseException {
        if (keyword.equals("reserved")) {
            return ReserveEvent.class;
        }
        throw new JsonParseException("Could not find a matching type for the given json");
    }
    
}
