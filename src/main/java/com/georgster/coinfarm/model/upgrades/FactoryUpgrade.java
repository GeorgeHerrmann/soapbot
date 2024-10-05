package com.georgster.coinfarm.model.upgrades;

public abstract class FactoryUpgrade {
    
    private final String name;
    private final String trackName;
    private final String description;
    private final int level;
    private long cost;
    private boolean owned;

    protected FactoryUpgrade(String name, String trackName, String description, int level, long cost) throws IllegalArgumentException {
        this.name = name;
        this.trackName = trackName;
        this.description = description;
        this.level = level;
        this.cost = cost;
        this.owned = false;
    }

    public abstract void applyUpgrade(CoinProductionState state);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public long getCost() {
        return cost;
    }

    public void markAsOwned() {
        owned = true;
    }

    public void markAsUnowned() {
        owned = false;
    }

    public boolean isOwned() {
        return owned;
    }

    public long getRefundValue() {
        return cost / 2;
    }

    public String getTrackName() {
        return trackName;
    }

}
