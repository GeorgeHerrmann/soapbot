package com.georgster.coinfarm.model.upgrades;

import java.util.ArrayList;
import java.util.List;

/**
 * A state of a {@link com.georgster.coinfarm.model.CoinFactory CoinFactory} that holds the production values of the factory
 * during a production cycle. The state is modified by {@link FactoryUpgrade FactoryUpgrades} to change the production values.
 * <p>
 * A {@link CoinProductionState} has a starting production value, a base production value, and a working production value.
 * <p>
 * <i>The starting production value is the production value of the factory at the start of a production cycle and is not modified.
 * <p>
 * The base production value is the production value of the factory that is modified by multiplicative upgrades. Base production value
 * upgrades <b>also affect</b> the working production value.
 * <p>
 * The working production value is the production value of the factory that is modified by additive upgrades. Working production value
 * upgrades <b>DO NOT</b> affect the base production value.</i>
 */
public final class CoinProductionState {
    private final long startingProductionValue; // starting production value of the factory
    private long baseProductionValue; // base production value of the factory (for multiplicative upgrades)
    private long workingProductionValue; // production value of the factory while working (for additive upgrades)
    private final List<FactoryUpgrade> upgrades;

    /**
     * Constructs a new CoinProductionState with the given starting production value.
     * <p>
     * <b>This signifies the start of a coin production cycle.</b>
     * 
     * @param startingProductionValue The starting production value of the factory <i>(aka how many coins the factory has at the start of a cycle)</i>
     */
    public CoinProductionState(long startingProductionValue) {
        this.startingProductionValue = startingProductionValue;
        this.baseProductionValue = startingProductionValue;
        this.workingProductionValue = startingProductionValue;
        this.upgrades = new ArrayList<>();
    }

    /**
     * Adds the given upgrade to the list of upgrades in the state.
     * 
     * @param upgrade The upgrade to add to the state
     */
    public void addUpgrade(FactoryUpgrade upgrade) {
        upgrades.add(upgrade);
        upgrade.applyUpgrade(this);
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
        baseProductionValue += value;
        workingProductionValue += value;
    }

    /**
     * Adds to the working production value of the factory by the given value.
     * <p>
     * <i>Upgrading the working production value should generally only be used for additive upgrades.</i>
     * 
     * @param value The value to add to the working production value
     */
    public void upgradeWorkingProductionValue(long value) {
        workingProductionValue += value;
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
     * Finishes the production cycle and returns the production value of the factory during the cycle <i>(how many coins the factory produced during the cycle, excluding the starting production value)</i>.
     * 
     * @return The production value of the factory during the cycle
     */
    public long finishProductionCycle() {
        return workingProductionValue - startingProductionValue;
    }
}
