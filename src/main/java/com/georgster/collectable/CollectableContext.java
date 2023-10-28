package com.georgster.collectable;

import com.georgster.collectable.Collectable.Rarity;
import com.georgster.control.manager.UserProfileManager;

public class CollectableContext {
    private String name;
    private final String ownerId;
    private String description;
    private String imageUrl;
    private long cost;
    private long initialCost;

    public CollectableContext(String name, String ownerId, String description, String imageUrl, long initialCost) {
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = initialCost;
        this.initialCost = initialCost;
    }

    public CollectableContext(String name, String ownerId, String description, String imageUrl, long cost, long initialCost) {
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = cost;
        this.initialCost = initialCost;
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getCost() {
        return cost;
    }

    public long getInitialCost() {
        return initialCost;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public void setInitialCost(long initialCost) {
        this.initialCost = initialCost;
    }

    public static CollectableContext from(Collectable collectable) {
        return new CollectableContext(collectable.getName(), collectable.getCreatorId(), collectable.getDescription(), collectable.getImageUrl(), collectable.getInitialCost());
    }

    public Rarity getRarity(UserProfileManager manager) {
        long totalCoins = manager.getTotalCoins();
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
