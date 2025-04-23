package com.georgster.coinfactory.model.upgrades;

import java.util.List;

import com.georgster.coinfactory.model.CoinFactoryContext;

/**
 * A state of the coin production of a {@link CoinFactory} that is used to process upgrades and calculate production values.
 * <p>
 * The <b>STARTING</b> production value is the amount of processed coins that are produced at the start of a production cycle.
 * Starting production upgrades are processed first before working or base production upgrades and can be multiplicative or additive.
 * After all starting production upgrades have been processed, the base and working production values are set to the calculated starting production value.
 * <p>
 * The <b>BASE</b> production value is the amount of base coins that multiplicative upgrades will be based off for coin production.
 * Base production upgrades should only be additive and are <i>NOT</i> representative of the actual amount of coins produced in a cycle.
 * <p>
 * The <b>WORKING</b> production value is the actual amount of produced coins that are produced in a production cycle.
 * Working production upgrades should be multiplicative and are based off of the current base production value.
 * For Example: If the base production value is 50, and a working production upgrade is applied with a value of 0.5, 25 <i>(50 * .5)</i> coins will
 * be added to the working production.
 * <p>
 * The {@link CoinProductionState} is also responsible for applying prestige-level increases to the production values of the factory.
 * <p>
 * A {@code true process cycle} is a cycle where the state is being processed for a production cycle, and the resulting amount of working coins is
 * intended to be deposited into a {@link CoinFactory}, whereas a {@code simulation cycle} is a cycle where the state is being processed for a simulation of a production cycle,
 * and the resulting amount of working coins is not intended to be deposited into a {@link CoinFactory}.
 */
public final class CoinProductionState {
    public static final double PRESTIGE_MULTIPLIER = 0.1; // The multiplier for the prestige level of the factory

    private FactoryUpgrade currentlyProcessingUpgrade; // the upgrade currently being processed
    private final List<FactoryUpgrade> upgrades; // List of upgrades this factory has purchased

    private long startingProductionValue; // starting production value of the factory
    private long baseProductionValue; // base production value of the factory (for multiplicative upgrades)
    private long workingProductionValue; // production value of the factory while working (for additive upgrades)

    private long lowestPoissbleBaseValue; // the lowest possible base production value of the factory (for coin projections)
    private long highestPossibleBaseValue; // the highest possible base production value of the factory (for coin projections)
    private long lowestPossibleWorkingValue; // the lowest possible working production value of the factory (for coin projections)
    private long highestPossibleWorkingValue; // the highest possible working production value of the factory (for coin projections)

    private boolean hasProcessedStartingModifiers; // whether the state has processed any upgrades that modify the starting production value (they go first)
    private final boolean isTrueProcessCycle; // whether the state is in a true processing cycle (true) or a simulation cycle (false)
    
    private final CoinFactoryContext factoryContext;

    /**
     * Creates a new {@link CoinProductionState} with the given factory context and whether the state is in a true processing cycle.
     * 
     * @param factoryContext The context of the {@link CoinFactory} that this state will process coins for.
     * @param isTrueProcessCycle Whether the state is in a true processing cycle (true) or a simulation cycle (false)
     */
    public CoinProductionState(CoinFactoryContext factoryContext, boolean isTrueProcessCycle) {
        this.factoryContext = factoryContext;
        this.upgrades = factoryContext.getUpgrades();
        this.isTrueProcessCycle = isTrueProcessCycle;
        this.startingProductionValue = 1;
        this.baseProductionValue = startingProductionValue; // base production value of the factory (for multiplicative upgrades)
        this.workingProductionValue = startingProductionValue; // production value of the factory while working (for additive upgrades)
        this.lowestPossibleWorkingValue = startingProductionValue; // the lowest possible working production value of the factory
        this.highestPossibleWorkingValue = startingProductionValue; // the highest possible working production value of the factory
        this.lowestPoissbleBaseValue = startingProductionValue; // the lowest possible base production value of the factory
        this.highestPossibleBaseValue = startingProductionValue; // the highest possible base production value of the factory
        this.hasProcessedStartingModifiers = false; // whether the state has processed any upgrades that modify the starting production value (they go first)
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
     * Returns the lowest possible base production value of the factory.
     * This value is used to determine the lowest possible production value of the factory during a production cycle.
     * <p>
     * <i>Generally used for upgrades with random chances.</i>
     * 
     * @return The lowest possible base production value of the factory
     */
    public long getLowestPossibleBaseValue() {
        return lowestPoissbleBaseValue;
    }

    /**
     * Returns the highest possible base production value of the factory.
     * This value is used to determine the highest possible production value of the factory during a production cycle.
     * <p>
     * <i>Generally used for upgrades with random chances.</i>
     * 
     * @return The highest possible base production value of the factory
     */
    public long getHighestPossibleBaseValue() {
        return highestPossibleBaseValue;
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
        return factoryContext.getPrestige();
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
        lowestPoissbleBaseValue = startingProductionValue;
        highestPossibleBaseValue = startingProductionValue;
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

    /**
     * Increases the base production value by the given {@code value}.
     * 
     * @param value The value to increase the base production value by
     */
    public void upgradeBaseProduction(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the base production value

        if (hasProcessedStartingModifiers) {
            baseProductionValue += value;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPoissbleBaseValue += value; // Update the lowest possible base production value
                highestPossibleBaseValue += value; // Update the highest possible base production value
            }
        }

        lowestPoissbleBaseValue = Math.max(lowestPoissbleBaseValue, 0); // Ensure the lowest possible base production value is at least 1
        highestPossibleBaseValue = Math.max(highestPossibleBaseValue, 0); // Ensure the highest possible base production value is at least 1
        baseProductionValue = Math.max(baseProductionValue, 0);
    }

    /**
     * Increases the working production value based off the given {@code multiplier}.
     * <p>
     * The multiplier is applied to the base production value to calculate the additive working value.
     * <p>
     * For example: A multiplier of 0.5 will add 50% of the base production value to the working production value.
     * 
     * @param multiplier The multiplier to apply to the base production value to calculate the additive working value
     */
    public void upgradeWorkingProduction(double multiplier) {
        if (hasProcessedStartingModifiers) {
            long additiveValue = (long) (baseProductionValue * multiplier); // Calculate the additive value based on the base production value
            workingProductionValue += additiveValue;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue += (long) (lowestPoissbleBaseValue * multiplier); // Update the lowest possible working value
                highestPossibleWorkingValue += (long) (highestPossibleBaseValue * multiplier); // Update the highest possible working value
            }

            //ensure values dont go below 0
            lowestPossibleWorkingValue = Math.max(lowestPossibleWorkingValue, 0);
            highestPossibleWorkingValue = Math.max(highestPossibleWorkingValue, 0);
            workingProductionValue = Math.max(workingProductionValue, 0);
        }
    }

    /**
     * Increases the working production value by the given {@code value}.
     * 
     * @param value The value to increase the working production value by
     */
    public void upgradeStartingProduction(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the starting production value
        
        if (!hasProcessedStartingModifiers) {
            startingProductionValue += value;
        }

        startingProductionValue = Math.max(startingProductionValue, 0); // Ensure the starting production value is at least 0
    }

    /**
     * Increases the starting production value based off the given {@code multiplier}.
     * <p>
     * The multiplier is applied to the starting production value to calculate the additive starting value.
     * <p>
     * For example: A multiplier of 0.5 will add 50% of the starting production value to the starting production value.
     * 
     * @param multiplier The multiplier to apply to the starting production value to calculate the additive starting value
     */
    public void upgradeStartingProduction(double multiplier) {
        if (!hasProcessedStartingModifiers) {
            long additiveValue = (long) (startingProductionValue * multiplier);
            startingProductionValue += additiveValue;

            startingProductionValue = Math.max(startingProductionValue, 0); // Ensure the starting production value is at least 0
        }
    }

    /**
     * Decreases the starting production value by the given {@code value}.
     * 
     * @param value The value to decrease the starting production value by
     */
    public void decreaseStartingProduction(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the starting production value
        
        if (!hasProcessedStartingModifiers) {
            startingProductionValue -= value;
        }

        startingProductionValue = Math.max(startingProductionValue, 0); // Ensure the starting production value is at least 1
    }

    /**
     * Decreases the base production value by the given {@code value}.
     * 
     * @param value The value to decrease the base production value by
     */
    public void decreaseBaseProduction(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the base production value

        if (hasProcessedStartingModifiers) {
            baseProductionValue -= value;
        }

        if (!currentlyProcessingUpgrade.hasRandomChance()) {
            lowestPoissbleBaseValue -= value; // Update the lowest possible base production value
            highestPossibleBaseValue -= value; // Update the highest possible base production value
        }

        lowestPoissbleBaseValue = Math.max(lowestPoissbleBaseValue, 0); // Ensure the lowest possible base production value is at least 1
        highestPossibleBaseValue = Math.max(highestPossibleBaseValue, 0); // Ensure the highest possible base production value is at least 1
        baseProductionValue = Math.max(baseProductionValue, 0); // Ensure the base production value is at least 1
    }

    /**
     * Decreases the working production value by the given {@code value}.
     * <p>
     * This decreases working production by a static amount. To decrease working production by a percentage of the base production value, use {@link #wipeCoins(double)}.
     * 
     * @param value The value to decrease the working production value by
     * @see #wipeCoins(double)
     */
    public void decreaseWorkingProduction(long value) {
        if (hasProcessedStartingModifiers) {
            workingProductionValue -= value;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue -= value; // Update the lowest possible working value
                highestPossibleWorkingValue -= value; // Update the highest possible working value
            }
        }

        lowestPossibleWorkingValue = Math.max(lowestPossibleWorkingValue, 0); // Ensure the lowest possible working value is at least 1
        highestPossibleWorkingValue = Math.max(highestPossibleWorkingValue, 0); // Ensure the highest possible working value is at least 1
        workingProductionValue = Math.max(workingProductionValue, 0); // Ensure the working production value is at least 1
    }

    /**
     * Wipes the working production value by the given {@code percentage}.
     * 
     * @param percentage The percentage to wipe the working production value by
     */
    public void wipeCoins(double percentage) {
        if (hasProcessedStartingModifiers) {
            long wipeValue = (long) (workingProductionValue * percentage); // Calculate the wipe value based on the working production value
            workingProductionValue -= wipeValue;

            if (!currentlyProcessingUpgrade.hasRandomChance()) {
                lowestPossibleWorkingValue -= wipeValue; // Update the lowest possible working value
                highestPossibleWorkingValue -= wipeValue; // Update the highest possible working value
            }
        }

        lowestPossibleWorkingValue = Math.max(lowestPossibleWorkingValue, 0); // Ensure the lowest possible working value is at least 1
        highestPossibleWorkingValue = Math.max(highestPossibleWorkingValue, 0); // Ensure the highest possible working value is at least 1
        workingProductionValue = Math.max(workingProductionValue, 0); // Ensure the working production value is at least 1
    }

    /**
     * Increases the lowest possible working value of the factory by the given {@code value}.
     * <p>
     * The lowest and highest possible working values are <b>NOT</b> representative of the actual amount of coins produced in a cycle,
     * rather are used to gather a range of possible production values for upgrades with random chances.
     * 
     * @param value The value to increase the lowest possible working value by
     */
    public void registerLowestPossibleWorkingValue(long value) {
        if (hasProcessedStartingModifiers) {
            lowestPossibleWorkingValue += value; // Update the lowest possible working value
        }
    }

    /**
     * Increases the highest possible working value of the factory by the given {@code value}.
     * <p>
     * The lowest and highest possible working values are <b>NOT</b> representative of the actual amount of coins produced in a cycle,
     * rather are used to gather a range of possible production values for upgrades with random chances.
     * 
     * @param value The value to increase the highest possible working value by
     */
    public void registerHighestPossibleWorkingValue(long value) {
        if (hasProcessedStartingModifiers) {
            highestPossibleWorkingValue += value; // Update the highest possible working value
        }
    }

    /**
     * Registers a possible percentage-based working value to the highest possible working value of the factory.
     * <p>
     * The lowest and highest possible working values are <b>NOT</b> representative of the actual amount of coins produced in a cycle,
     * rather are used to gather a range of possible production values for upgrades with random chances.
     * 
     * @param percentage The percentage to register as a possible percentage-based working value
     */
    public void registerHighestPossibleWorkingValue(double percentage) {
        if (hasProcessedStartingModifiers) {
            long additiveValue = (long) (highestPossibleBaseValue * percentage); // Calculate the additive value based on the base production value
            highestPossibleWorkingValue += additiveValue;
            highestPossibleWorkingValue = Math.max(highestPossibleWorkingValue, 0);
        }
    }

    /**
     * Registers a possible percentage-based working value to the lowest possible working value of the factory.
     * <p>
     * The lowest and highest possible working values are <b>NOT</b> representative of the actual amount of coins produced in a cycle,
     * rather are used to gather a range of possible production values for upgrades with random chances.
     * 
     * @param percentage The percentage to register as a possible percentage-based working value
     */
    public void registerLowestPossibleWorkingValue(double percentage) {
        if (hasProcessedStartingModifiers) {
            long additiveValue = (long) (lowestPoissbleBaseValue * percentage); // Calculate the additive value based on the base production value
            lowestPossibleWorkingValue += additiveValue;
            lowestPossibleWorkingValue = Math.max(lowestPossibleWorkingValue, 0);
        }
    }

    /**
     * Registers a possible static-amount base value to the lowest possible base value of the factory.
     * <p>
     * The lowest and highest possible base values are <b>NOT</b> representative of the actual amount of coins produced in a cycle,
     * rather are used to gather a range of possible production values for upgrades with random chances.
     * 
     * @param value The value to register as a possible static-amount base value
     */
    public void registerLowestPossibleBaseValue(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the lowest possible base value

        if (hasProcessedStartingModifiers) {
            lowestPoissbleBaseValue += value; // Update the lowest possible base value
        }
    }

    /**
     * Registers a possible static-amount base value to the highest possible base value of the factory.
     * <p>
     * The lowest and highest possible base values are <b>NOT</b> representative of the actual amount of coins produced in a cycle,
     * rather are used to gather a range of possible production values for upgrades with random chances.
     * 
     * @param value The value to register as a possible static-amount base value
     */
    public void registerHighestPossibleBaseValue(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the highest possible base value

        if (hasProcessedStartingModifiers) {
            highestPossibleBaseValue += value; // Update the highest possible base value
        }
    }

    /**
     * Registers a possible static-amount coin wipe value to the lowest possible working value of the factory.
     * 
     * @param value The value to register as a possible static-amount coin wipe value
     */
    public void registerPossibleCoinWipe(long value) {
        value *= (getPrestigeLevel() * PRESTIGE_MULTIPLIER) + 1; // Apply the prestige multiplier to the possible coin wipe value

        if (hasProcessedStartingModifiers) {
            lowestPossibleWorkingValue = Math.max(0, lowestPossibleWorkingValue - value);
        }
    }

    /**
     * Registers a possible percentage-based coin wipe value to the lowest possible working value of the factory.
     * 
     * @param percentage The percentage to register as a possible percentage-based coin wipe value
     */
    public void registerPossibleCoinWipe(double percentage) {
        if (hasProcessedStartingModifiers) {
            long wipeValue = (long) (workingProductionValue * percentage); // Calculate the wipe value based on the working production value
            lowestPossibleWorkingValue = Math.max(0, lowestPossibleWorkingValue - wipeValue); // Update the lowest possible working value
        }
    }
}
