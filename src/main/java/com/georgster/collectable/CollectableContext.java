package com.georgster.collectable;

import com.georgster.collectable.Collectable.Rarity;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;

/**
 * A context for a {@link Collectable}.
 */
public class CollectableContext {
    private String name;
    private final String ownerId;
    private String description;
    private String imageUrl;
    private long cost;
    private long initialCost;

    /**
     * Constructs a {@link CollectableContext} with the given parameters.
     * 
     * @param name The name of the {@code Collectable}.
     * @param ownerId The {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that created the {@code Collectable}.
     * @param description The description of the {@code Collectable}.
     * @param imageUrl The url of the image of the {@code Collectable}.
     * @param initialCost The initial cost of the {@code Collectable}.
     */
    public CollectableContext(String name, String ownerId, String description, String imageUrl, long initialCost) {
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = initialCost;
        this.initialCost = initialCost;
    }

    /**
     * Constructs a {@link CollectableContext} with the given parameters.
     * 
     * @param name The name of the {@code Collectable}.
     * @param ownerId The {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that created the {@code Collectable}.
     * @param description The description of the {@code Collectable}.
     * @param imageUrl The url of the image of the {@code Collectable}.
     * @param cost The cost of the {@code Collectable}.
     * @param initialCost The initial cost of the {@code Collectable}.
     */
    public CollectableContext(String name, String ownerId, String description, String imageUrl, long cost, long initialCost) {
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = cost;
        this.initialCost = initialCost;
    }

    /**
     * Returns the name of the {@link Collectable}.
     * 
     * @return the name of the {@code Collectable}.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that created the {@link Collectable}.
     * 
     * @return the {@code id} of the {@code UserProfile} that created the {@code Collectable}.
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Returns the description of the {@link Collectable}.
     * 
     * @return the description of the {@code Collectable}.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the url of the image of the {@link Collectable}.
     * 
     * @return the url of the image of the {@code Collectable}.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Returns the cost of the {@link Collectable}.
     * 
     * @return the cost of the {@code Collectable}.
     */
    public long getCost() {
        return cost;
    }

    /**
     * Returns the initial cost of the {@link Collectable}.
     * 
     * @return the initial cost of the {@code Collectable}.
     */
    public long getInitialCost() {
        return initialCost;
    }

    /**
     * Sets the name of the {@link Collectable}.
     * 
     * @param name The new name of the {@code Collectable}.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description of the {@link Collectable}.
     * 
     * @param description The new description of the {@code Collectable}.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the url of the image of the {@link Collectable}.
     * 
     * @param imageUrl The new url of the image of the {@code Collectable}.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Sets the cost of the {@link Collectable}.
     * 
     * @param cost The new cost of the {@code Collectable}.
     */
    public void setCost(long cost) {
        this.cost = cost;
    }

    /**
     * Sets the initial cost of the {@link Collectable}.
     * 
     * @param initialCost The new initial cost of the {@code Collectable}.
     */
    public void setInitialCost(long initialCost) {
        this.initialCost = initialCost;
    }

    /**
     * Returns a {@link CollectableContext} from the given {@link Collectable}.
     * 
     * @param collectable The {@code Collectable} to get the {@code CollectableContext} from.
     * @return a {@code CollectableContext} from the given {@code Collectable}.
     */
    public static CollectableContext from(Collectable collectable) {
        return new CollectableContext(collectable.getName(), collectable.getCreatorId(), collectable.getDescription(), collectable.getImageUrl(), collectable.getInitialCost());
    }

    /**
     * Returns the {@link Rarity} of the {@link Collectable} with the given {@link CollectableContext}.
     * 
     * @param manager The {@link UserProfileManager} to get the total coins from.
     * @param collectableManager The {@code CollectableManager} to get the total coins from.
     * @return the {@code Rarity} of the {@code Collectable} with the given {@code CollectableContext}.
     */
    public Rarity getRarity(UserProfileManager manager, CollectableManager collectableManager) {
        long totalCoins = manager.getTotalCoins() + collectableManager.getTotalCoins();
        if (getCost() >= totalCoins * .25) {
            return Rarity.UNIQUE;
        } else if (getCost() >= totalCoins * .1) {
            return Rarity.LEGENDARY;
        } else if (getCost() >= totalCoins * .05) {
            return Rarity.RARE;
        } else if (getCost() >= totalCoins * .01) {
            return Rarity.UNCOMMON;
        } else {
            return Rarity.COMMON;
        }
    }
}
