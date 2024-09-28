package com.georgster.coinfarm.model.upgrades;

public final class CoinProductionState {
    private final long startingProductionValue; // starting production value of the factory
    private long baseProductionValue; // base production value of the factory (for multiplicative upgrades)
    private long workingProductionValue; // production value of the factory while working (for additive upgrades)

    public CoinProductionState(long startingProductionValue) {
        this.startingProductionValue = startingProductionValue;
        this.baseProductionValue = startingProductionValue;
        this.workingProductionValue = startingProductionValue;
    }

    public void upgradeBaseProductionValue(long value) {
        baseProductionValue += value;
        workingProductionValue += value;
    }

    public void upgradeWorkingProductionValue(long value) {
        workingProductionValue += value;
    }

    public long getStartingProductionValue() {
        return startingProductionValue;
    }

    public long getBaseProductionValue() {
        return baseProductionValue;
    }

    public long getWorkingProductionValue() {
        return workingProductionValue;
    }

    public long finishProductionCycle() {
        return workingProductionValue - startingProductionValue;
    }
}
