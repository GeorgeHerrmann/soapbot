package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.collectable.Collectable;
import com.georgster.collectable.Collected;
import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.util.Unwrapper;

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

    /**
     * Updates all {@link Collectable Collectables} from the given {@link UserProfileManager}.
     * 
     * @param manager the manager to update from
     */
    public void updateFromProfiles(UserProfileManager manager) {
        manager.getAll().forEach(profile -> {
            Unwrapper<Collectable> c = new Unwrapper<>();
            profile.getCollecteds().forEach(collected -> {
                c.setObject(get(collected.getName()));
                c.getObject().setContext(collected.getCollectable());
            });
            update(c.getObject());
        });
    }

    /**
     * Gets all {@link Collected Collecteds} from all {@link Collectable Collectables} in this manager.
     * 
     * @return a list of all {@code Collecteds}.
     */
    public List<Collected> getAllCollecteds() {
        List<Collected> returns = new ArrayList<>();
        getAll().forEach(collectable -> returns.addAll(collectable.getCollecteds()));
        return returns;
    }
}
