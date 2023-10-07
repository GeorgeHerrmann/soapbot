package com.georgster.collectable;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.identify.util.UniqueIdentified;

public final class Collectable extends UniqueIdentified {
    private String name;
    private String description;
    private String imageUrl;
    private long cost;
    private final long initialCost;
    private final List<Collected> collecteds;

    // new
    public Collectable(String name, String description, String imageUrl, long cost) {
        super(name);
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cost = cost;
        this.initialCost = cost;
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

    public List<Collected> getCollecteds() {
        return collecteds;
    }

    public void addCollected(String memberId, long initialPurchasePrice) {
        collecteds.add(new Collected(memberId, initialPurchasePrice, this));
        this.cost /= 2;
    }




}
