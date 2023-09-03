package com.georgster.control.util.identify.util;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.UniqueIdBased;

/**
 * A {@link Manageable} that is identified by a unique ID.
 */
public abstract class UniqueIdentified extends UniqueIdBased implements Manageable {
    protected UniqueIdentified(String id) {
        super(id);
    }

    /**
     * Creates a new UniqueIdentified with a fresh, randomly generated ID.
     */
    protected UniqueIdentified() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
         return getId();
    }
}
