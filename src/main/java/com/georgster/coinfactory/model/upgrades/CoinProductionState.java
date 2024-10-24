package com.georgster.coinfactory.model.upgrades;

import java.util.ArrayList;
import java.util.List;

/**
 * A state of a {@link com.georgster.coinfactory.model.CoinFactory CoinFactory} that holds the production values of the factory
 * during a production cycle. The state is modified by {@link FactoryUpgrade FactoryUpgrades} to change the production values.
 * <p>
 * A {@link CoinProductionState} has a starting production value, a base production value, and a working production value.
 * <p>
 * <i>The starting production value is the production value of the factory at the start of a production cycle and is not modified unless decreased by an upgrade.
 * <p>
 * The base production value is the production value of the factory that is modified by multiplicative upgrades. Base production value
 * upgrades <b>also affect</b> the working production value.
 * <p>
 * The working production value is the production value of the factory that is modified by additive upgrades. Working production value
 * upgrades <b>DO NOT</b> affect the base production value.</i>
 */
public final class CoinProductionState {
    private long startingProductionValue; // starting production value of the factory
    private long baseProductionValue; // base production value of the factory (for multiplicative upgrades)
    private long workingProductionValue; // production value of the factory while working (for additive upgrades)
    private final List<FactoryUpgrade> upgrades;
    private final int prestigeLevel; // the prestige level of the factory

    private boolean hasProcessedStartingModifiers; // whether the state has processed any upgrades that modify the starting production value (they go first)

    /**
     * Constructs a new CoinProductionState
     * <p>
     * <b>This signifies the start of a coin production cycle.</b>
     */
    public CoinProductionState(List<FactoryUpgrade> upgrades, int prestigeLevel) {
        this.startingProductionValue = 1;
        this.baseProductionValue = startingProductionValue;
        this.workingProductionValue = startingProductionValue;
        this.upgrades = new ArrayList<>(upgrades);
        this.prestigeLevel = prestigeLevel;
        this.hasProcessedStartingModifiers = false;
    }

    /**
     * Adds the given upgrade to the list of upgrades in the state.
     * 
     * @param upgrade The upgrade to add to the state
     */
    public void addUpgrade(FactoryUpgrade upgrade) {
        upgrades.add(upgrade);
    }

    /**
     * Sets the list of upgrades in the state to the given list of upgrades.
     * 
     * @param upgrades The list of upgrades to set in the state
     */
    public void setUpgrades(List<FactoryUpgrade> upgrades) {
        this.upgrades.clear();
        this.upgrades.addAll(upgrades);
    }

    /**
     * Returns the list of upgrades in the state.
     * 
     * @return The list of upgrades in the state
     */
    public List<FactoryUpgrade> getUpgrades() {
        return upgrades;
    }

    /**
     * Adds to the base production value of the factory by the given value.
     * <p>
     * <i>Upgrading the base production value should generally only be used for multiplicative upgrades.</i>
     * 
     * @param value The value to add to the base production value
     */
    public void upgradeBaseProductionValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (hasProcessedStartingModifiers) {
            baseProductionValue += value;
            workingProductionValue += value;
        }
    }

    /**
     * Adds to the working production value of the factory by the given value.
     * <p>
     * <i>Upgrading the working production value should generally only be used for additive upgrades.</i>
     * 
     * @param value The value to add to the working production value
     */
    public void upgradeWorkingProductionValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (hasProcessedStartingModifiers) {
            workingProductionValue += value;
        }
    }

    /**
     * Adds to the starting production value of the factory by the given value.
     * 
     * @param value The value to add to the starting production value
     */
    public void upgradeStartingProductionValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (!hasProcessedStartingModifiers) {
            startingProductionValue += value;
        }
    }

    /**
     * Decreases the starting production value of the factory by the given value.
     * 
     * @param value The value to decrease the starting production value by
     */
    public void decreaseStartingProductionValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (!hasProcessedStartingModifiers) {
            startingProductionValue -= value;
        }

        startingProductionValue = Math.max(0, startingProductionValue);
    }

    /**
     * Decreases the working production value of the factory by the given value.
     * 
     * @param value The value to decrease the working production value by
     */
    public void decreaseWorkingProductionValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (!hasProcessedStartingModifiers) {
            workingProductionValue -= value;
        }

        // set to zero if negative, or keep value if positive
        workingProductionValue = Math.max(0, workingProductionValue);
    }
    
    /**
     * Decreases the base production value of the factory by the given value.
     * 
     * @param value The value to decrease the base production value by
     */
    public void decreaseBaseProductionValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (!hasProcessedStartingModifiers) {
            baseProductionValue -= value;
            workingProductionValue -= value;
        }

        // set to zero if negative, or keep value if positive
        baseProductionValue = Math.max(0, baseProductionValue);
        workingProductionValue = Math.max(0, workingProductionValue);
    }

    /**
     * Wipes the given value of coins from the factory, draining the base and working production values first, then the starting production value if needed.
     * <p>
     * <i>Values should not go negative and should stop at zero no matter how large the value is.</i>
     * 
     * @param value The value of coins to wipe from the factory
     */
    public void wipeCoins(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (!hasProcessedStartingModifiers) {
            return;
        }

        // wipe from working and base production values, ensuring they do not go below zero
        baseProductionValue = Math.max(0, baseProductionValue - value);
        workingProductionValue = Math.max(0, workingProductionValue - value);


    }

    /**
     * Returns the starting production value of the factory.
     * 
     * @return The starting production value of the factory
     */
    public long getStartingProductionValue() {
        return startingProductionValue;
    }

    /**
     * Returns the current base production value of the factory.
     * 
     * @return The current base production value of the factory
     */
    public long getBaseProductionValue() {
        return baseProductionValue;
    }

    /**
     * Returns the current working production value of the factory.
     * 
     * @return The current working production value of the factory
     */
    public long getWorkingProductionValue() {
        return workingProductionValue;
    }

    /**
     * Returns the total production value of the factory <i>(the sum of the starting production value and the working production value)</i>.
     * 
     * @return The total production value of the factory
     */
    public long getTotalCoins() {
        return startingProductionValue + workingProductionValue;
    }

    /**
     * Returns the prestige level of the factory.
     * 
     * @return The prestige level of the factory
     */
    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    /**
     * Marks the state as having processed the starting modifiers <i>(upgrades that modify the starting production value)</i>.
     * <p>
     * Upgrades which affect the starting production value should be processed first before any other upgrades.
     */
    public void markFirstBatchProcessed() {
        hasProcessedStartingModifiers = true;
        workingProductionValue = startingProductionValue;
        baseProductionValue = startingProductionValue;
    }

    /**
     * Returns whether the state has processed upgrades that modify the starting production value.
     * <p>
     * Upgrades which affect the starting production value will be processed first before any other upgrades,
     * any modifiers that affect the base or working production values will not be processed until all starting modifiers have been processed and
     * vice versa.
     * 
     * @return Whether the state has processed upgrades that modify the starting production value
     */
    public boolean hasProcessedStartingModifiers() {
        return hasProcessedStartingModifiers;
    }
}
