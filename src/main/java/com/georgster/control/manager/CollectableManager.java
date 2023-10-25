package com.georgster.control.manager;

import com.georgster.collectable.Collectable;
import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;

/**
 * A {@link SoapManager} that manages {@link Collectable Collectables} for a SoapClient.
 */
public class CollectableManager extends SoapManager<Collectable> {
    /**
     * Constructs a {@link CollectableManager} for the given {@link ClientContext}
     * 
     * @param context the context carrying the {@code Guild} to manage collectables for
     */
    public CollectableManager(ClientContext context) {
        super(context, ProfileType.COLLECTABLES, Collectable.class, "id");
    }
}
