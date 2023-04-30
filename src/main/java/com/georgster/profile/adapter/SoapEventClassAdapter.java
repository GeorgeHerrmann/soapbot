package com.georgster.profile.adapter;

import com.georgster.events.SoapEvent;
import com.georgster.events.reserve.ReserveEvent;
import com.google.gson.JsonParseException;

/**
 * An adapter that allows for the deserialization of inherited {@code SoapEvent}s.
 */
public class SoapEventClassAdapter extends DatabaseObjectClassAdapter<SoapEvent> {

    /** 
     * Constructs a {@code SoapEventClassAdapter}.
     */
    public SoapEventClassAdapter() {
        super("reserved");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends SoapEvent> getType(String keyword) throws JsonParseException {
        if (keyword.equals("reserved")) {
            return ReserveEvent.class;
        }
        throw new JsonParseException("Could not find a matching type for the given json");
    }
    
}
