package com.georgster.control.util.identify;

/**
 * An {@link IdBased} object that is identified by a unique Id.
 */
public abstract class UniqueIdBased implements IdBased {
    private String id;

    protected UniqueIdBased(String id) {
        this.id = id;
    }

    /**
     * Creates a new UniqueIdBased with a fresh, randomly generated ID.
     */
    protected UniqueIdBased() {
        this.id = UniqueIdFactory.createId();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }
}
