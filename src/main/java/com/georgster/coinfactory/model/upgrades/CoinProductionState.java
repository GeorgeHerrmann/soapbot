package com.georgster.coinfactory.model.upgrades;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private FactoryUpgrade currentlyProcessingUpgrade; // the upgrade currently being processed

    private long startingProductionValue; // starting production value of the factory
    private long baseProductionValue; // base production value of the factory (for multiplicative upgrades)
    private long workingProductionValue; // production value of the factory while working (for additive upgrades)
    private final List<FactoryUpgrade> upgrades;
    private final int prestigeLevel; // the prestige level of the factory

    private long lowestPossibleWorkingValue; // the lowest possible working production value of the factory
    private long highestPossibleWorkingValue; // the highest possible working production value of the factory

    private boolean hasProcessedStartingModifiers; // whether the state has processed any upgrades that modify the starting production value (they go first)
    private final boolean isTrueProcessCycle; // whether the state is in a true processing cycle (true) or a simulation cycle (false)

    /**
     * Constructs a new CoinProductionState
     * <p>
     * <b>This signifies the start of a coin production cycle.</b>
     */
    public CoinProductionState(List<FactoryUpgrade> upgrades, int prestigeLevel, final boolean isTrueProcessCycle) {
        this.currentlyProcessingUpgrade = new AbsentFactoryUpgrade();
        this.startingProductionValue = 1;
        this.lowestPossibleWorkingValue = startingProductionValue;
        this.highestPossibleWorkingValue = startingProductionValue;
        this.baseProductionValue = startingProductionValue;
        this.workingProductionValue = startingProductionValue;
        this.upgrades = new ArrayList<>(upgrades);
        this.prestigeLevel = prestigeLevel;
        this.hasProcessedStartingModifiers = false;
        this.isTrueProcessCycle = isTrueProcessCycle;
    }

    /**
     * Processes the state by applying all upgrades in the state to the production values of the factory.
     */
    public void processUpgrades() {
        upgrades.forEach(upgrade -> {
            currentlyProcessingUpgrade = upgrade;
            upgrade.applyUpgrade(this);
        });
        markFirstBatchProcessed();
        upgrades.forEach(upgrade -> {
            currentlyProcessingUpgrade = upgrade;
            upgrade.applyUpgrade(this);
        });
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

        double multiplier = round(value / (double) baseProductionValue, 2);

        if (hasProcessedStartingModifiers) {
            baseProductionValue += value;
            workingProductionValue += value;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue += (lowestPossibleWorkingValue * multiplier);
                highestPossibleWorkingValue += (highestPossibleWorkingValue * multiplier);
            }
        }
    }

    /**
     * Rounds the given double to the given number of decimal places.
     * 
     * @param value The value to round
     * @param places The number of decimal places to round to
     * @return The rounded double
     * @throws IllegalArgumentException If the number of decimal places is negative
     */
    private static double round(double value, int places) throws IllegalArgumentException{
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue += value;
                highestPossibleWorkingValue += value;
            }
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

        if (hasProcessedStartingModifiers) {
            workingProductionValue -= value;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue -= value;
                highestPossibleWorkingValue -= value;
            }
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

        if (hasProcessedStartingModifiers) {
            baseProductionValue -= value;
            workingProductionValue -= value;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue -= value;
                highestPossibleWorkingValue -= value;
            }
        }

        // set to zero if negative, or keep value if positive
        baseProductionValue = Math.max(0, baseProductionValue);
        workingProductionValue = Math.max(0, workingProductionValue);
    }

    /**
     * Wipes the given value of coins from the factory's production values.
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
     * Wipes a percentage of the total coins from the factory's production values.
     * <p>
     * <i>Values should not go negative and should stop at zero no matter how large the value is.</i>
     * 
     * @param percentage The percentage of the total coins to wipe from the factory
     */
    public void wipeCoinsPercentage(double percentage) {
        if (!hasProcessedStartingModifiers) {
            return;
        }

        baseProductionValue = Math.max(0, (long) (baseProductionValue - (baseProductionValue * percentage)));
        workingProductionValue = Math.max(0, (long) (workingProductionValue - (workingProductionValue * percentage)));
    }

    /**
     * Registers the lowest possible working production value of the factory.
     * <p>
     * This will add the given value to the current lowest possible working production value.
     * 
     * @param value The value to register as the lowest possible working production value
     */
    public void registerLowestPossibleWorkingValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (hasProcessedStartingModifiers) {
            lowestPossibleWorkingValue += value;
        }
    }

    /**
     * Registers a possible wipe of {@code value} coins from the factory during a production cycle.
     * <p>
     * This will subtract the given value from the current lowest and highest possible working production values.
     * <p>
     * This method does <b>NOT</b> wipe coins from the factory, it only registers the possible wipe.
     * Use {@link CoinProductionState#wipeCoins(long)} to properly wipe coins from the factory.
     * 
     * @param value The value to register as a possible coin wipe
     */
    public void registerPossibleCoinWipe(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (!hasProcessedStartingModifiers) {
            return;
        }

        lowestPossibleWorkingValue = Math.max(0, lowestPossibleWorkingValue - value);
        //highestPossibleWorkingValue = Math.max(0, highestPossibleWorkingValue - value);
    }

    /**
     * Registers a possible wipe of a percentage of the total coins from the factory during a production cycle.
     * <p>
     * This will subtract the given percentage of the total coins from the current lowest and highest possible working production values.
     * <p>
     * This method does <b>NOT</b> wipe coins from the factory, it only registers the possible wipe.
     * Use {@link CoinProductionState#wipeCoinsPercentage(double)} to properly wipe coins from the factory.
     * 
     * @param percentage The percentage of the total coins to register as a possible coin wipe
     */
    public void registerPossibleCoinPercentageWipe(double percentage) {
        if (!hasProcessedStartingModifiers) {
            return;
        }

        long lowestValue = (long) (lowestPossibleWorkingValue * percentage);
        //long highestValue = (long) (highestPossibleWorkingValue * percentage);
        lowestPossibleWorkingValue = Math.max(0, lowestPossibleWorkingValue - lowestValue);
        //highestPossibleWorkingValue = Math.max(0, highestPossibleWorkingValue - highestValue);
    }

    /**
     * Registers the highest possible working production value of the factory.
     * <p>
     * This will add the given value to the current highest possible working production value.
     * 
     * @param value The value to register as the highest possible working production value
     */
    public void registerHighestPossibleWorkingValue(long value) {
        value *= (prestigeLevel * 0.1) + 1;

        if (hasProcessedStartingModifiers) {
            highestPossibleWorkingValue += value;
        }
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
     * Returns the lowest possible working production value of the factory.
     * This value is used to determine the lowest possible production value of the factory during a production cycle.
     * <p>
     * <i>Generally used for upgrades with random chances.</i>
     * 
     * @return The lowest possible working production value of the factory
     */
    public long getLowestPossibleWorkingValue() {
        return lowestPossibleWorkingValue;
    }

    /**
     * Returns the highest possible working production value of the factory.
     * This value is used to determine the highest possible production value of the factory during a production cycle.
     * <p>
     * <i>Generally used for upgrades with random chances.</i>
     * 
     * @return The highest possible working production value of the factory
     */
    public long getHighestPossibleWorkingValue() {
        return highestPossibleWorkingValue;
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
     * Returns the number of upgrades in the state.
     * 
     * @return The number of upgrades in the state
     */
    public int getUpgradeCount() {
        return upgrades.size();
    }

    /**
     * Returns the upgrade currently being processed, or the last processed upgrade if a full process cycle was already completed with this state.
     * <p>
     * If a process cycle has not started and an upgrade has not been processed, an {@link AbsentFactoryUpgrade} is returned.
     *  
     * @return The upgrade currently being processed, the last processed upgrade if a full process cycle was already completed with this state, or a {@link AbsentFactoryUpgrade} if no upgrades have been processed.
     */
    public FactoryUpgrade getCurrentlyProcessingUpgrade() {
        return currentlyProcessingUpgrade;
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
        lowestPossibleWorkingValue = startingProductionValue;
        highestPossibleWorkingValue = startingProductionValue;
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

    /**
     * Returns whether the state is in a true processing cycle.
     * <p>
     * A true processing cycle is a cycle where the state is being processed for a production cycle.
     * <p>
     * A simulation cycle is a cycle where the state is being processed for a simulation of a production cycle.
     * 
     * @return Whether the state is in a true processing cycle
     */
    public boolean isTrueProcessCycle() {
        return isTrueProcessCycle;
    }
}
