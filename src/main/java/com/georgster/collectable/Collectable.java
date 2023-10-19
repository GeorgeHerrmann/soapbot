package com.georgster.collectable;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.identify.util.UniqueIdentified;
import com.georgster.economy.exception.InsufficientCoinsException;
import com.georgster.profile.UserProfile;

public final class Collectable extends UniqueIdentified {
    private String name;
    private String description;
    private String imageUrl;
    private long cost;
    private long initialCost;
    private final List<Collected> collecteds;

    // new
    public Collectable(String name, String description, String imageUrl, long initialCost) {
        super(name);
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = initialCost;
        this.initialCost = initialCost;
        this.collecteds = new ArrayList<>();
    }

    // from database
    public Collectable(String name, String description, String imageUrl, long cost, long initialCost, List<Collected> collecteds) {
        super(name);
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = cost;
        this.initialCost = initialCost;
        this.collecteds = collecteds;
    }

    public String getName() {
        return name;
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

    public List<Collected> getCollecteds() {
        return collecteds;
    }

    public Collected getCollected(String id) {
        return collecteds.stream().filter(collected -> collected.getIdentifier().equals(id)).findFirst().orElse(null);
    }

    public static Collectable initialize(String name) {
        return new Collectable(name, "", "", 0);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        if (description == null) return;
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        if (imageUrl == null) return;
        this.imageUrl = imageUrl;
    }

    public void setInitialCost(long cost) {
        this.cost = cost;
        this.initialCost = cost;
    }

    public void purchaseCollected(UserProfile profile) throws InsufficientCoinsException {
        profile.getBank().withdrawl(cost); // Throws InsufficientCoinsException if not enough coins
        Collected collected = new Collected(profile.getMemberId(), cost, this);
        profile.addCollected(collected);
        collecteds.add(collected);
        if (!collecteds.isEmpty()) {
            this.cost /= 2;
        }
    }

    public void sellCollected(UserProfile profile, Collected collected) {
        profile.getBank().deposit(collected.getRecentPurchasePrice());
        profile.removeCollected(collected);
        collecteds.remove(collected);
        this.cost += collected.getRecentPurchasePrice();
    }

    public void sellCollected(UserProfile profile, String id) {
        Collected collected = profile.getCollecteds().stream().filter(collected1 -> collected1.getIdentifier().equals(id)).findFirst().orElse(null);
        if (collected == null) return;
        profile.getBank().deposit(collected.getRecentPurchasePrice());
        profile.removeCollected(collected);
        collecteds.remove(collected);
        this.cost += collected.getRecentPurchasePrice();
    }

}
