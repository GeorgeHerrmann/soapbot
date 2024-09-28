package com.georgster.coinfarm.model.upgrades;

public abstract class FactoryUpgrade {
    
    private final String name;
    private final int level;
    private long cost;
    private boolean owned;

    protected FactoryUpgrade(String name, int level, long cost) {
        this.name = name;
        this.level = level;
        this.cost = cost;
        this.owned = false;
    }

    public abstract void applyUpgrade(CoinProductionState state);

    public String getName() {
        return name;
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

    public boolean isOwned() {
        return owned;
    }

}
