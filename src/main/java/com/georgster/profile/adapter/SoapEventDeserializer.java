package com.georgster.profile.adapter;

import java.lang.reflect.Type;

import com.georgster.events.SoapEvent;
import com.georgster.events.reserve.ReserveEvent;

public class SoapEventDeserializer extends DatabaseObjectDeserializer<SoapEvent> {

    private SoapEvent object;
    
    public SoapEventDeserializer(SoapEvent object) {
        super(object);
    }

    public SoapEventDeserializer(String... uniqueIdentifiers) {
        super(uniqueIdentifiers);
    }
    
    @Override
    public Type getType() {
        if (object instanceof ReserveEvent) {
            return ReserveEvent.class;
        } else {
            return SoapEvent.class;
        }
    }
    
}
