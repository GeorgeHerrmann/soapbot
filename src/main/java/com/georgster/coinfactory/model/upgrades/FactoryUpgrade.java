package com.georgster.coinfactory.model.upgrades;

/**
 * An upgrade for a {@link com.georgster.coinfactory.model.CoinFactory CoinFactory} that can be applied to a {@link CoinProductionState}
 * to modify its production values during a production cycle.
 * <p>
 * Each upgrade has a name, a description, a level, a cost, and a track name and must define {@link #applyUpgrade(CoinProductionState)}
 * to modify the production values of a {@link CoinProductionState}, thereby "applying" the upgrade.
 */
public abstract class FactoryUpgrade {
    
    private final String name; // The name of the upgrade
    private final String trackName; // The name of the track the upgrade belongs to
    private final String description; // The description of the upgrade
    private final int level; // The level of the upgrade (within a FactoryUpgradeTrack)
    private long cost; // The cost of the upgrade
    private boolean owned; // Whether the upgrade is owned by the Member

    /**
     * Constructs a new FactoryUpgrade with the given name, track name, description, level, and cost.
     * 
     * @param name The name of the upgrade
     * @param trackName The name of the track the upgrade belongs to
     * @param description The description of the upgrade
     * @param level The level of the upgrade (within a FactoryUpgradeTrack)
     * @param cost The cost of the upgrade
     */
    protected FactoryUpgrade(String name, String trackName, String description, int level, long cost) {
        this.name = name;
        this.trackName = trackName;
        this.description = description;
        this.level = level;
        this.cost = cost;
        this.owned = false;
    }

    /**
     * Applies the upgrade to the given {@link CoinProductionState CoinProductionState}, modifying its production values during a production cycle.
     * 
     * @param state The CoinProductionState to apply the upgrade to
     */
    public abstract void applyUpgrade(CoinProductionState state);

    /**
     * Returns the name of the upgrade.
     * 
     * @return The name of the upgrade
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the upgrade.
     * 
     * @return The description of the upgrade
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the level of the upgrade (within a FactoryUpgradeTrack).
     * 
     * @return The level of the upgrade
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the cost of the upgrade given a prestige level.
     * <p>
     * <i>The cost is increased for each prestige level.</i>
     * 
     * @param prestigeLevel The prestige level of the factory
     * @return The cost of the upgrade
     */
    public long getCost(int prestigeLevel) {
        return (long) (cost * ((prestigeLevel * 0.25) + 1));
    }

    /**
     * Returns the base cost of the upgrade.
     * 
     * @return The base cost of the upgrade
     */
    public long getBaseCost() {
        return cost;
    }

    /**
     * Marks the upgrade as owned by the Member.
     */
    public void markAsOwned() {
        owned = true;
    }

    /**
     * Marks the upgrade as unowned by the Member.
     */
    public void markAsUnowned() {
        owned = false;
    }

    /**
     * Returns whether the upgrade is owned by the Member.
     * 
     * @return True if the upgrade is owned by the Member, false otherwise
     */
    public boolean isOwned() {
        return owned;
    }

    /**
     * Returns the refund value of the upgrade given the prestige level.
     * <p>
     * The floor is returned if the refund value is not a whole number.
     * 
     * @param prestigeLevel The prestige level of the factory
     * @return The refund value of the upgrade.
     */
    public long getRefundValue(int prestigeLevel) {
        return getCost(prestigeLevel) / 2;
    }

    /**
     * Returns the base refund value of the upgrade.
     * 
     * @return The base refund value of the upgrade
     */
    public long getBaseRefundValue() {
        return cost / 2;
    }

    /**
     * Returns the name of the track the upgrade belongs to.
     * 
     * @return The name of the track the upgrade belongs to
     */
    public String getTrackName() {
        return trackName;
    }

    /**
     * Returns whether the upgrade has a random chance for an effect to be applied.
     * <p>
     * Upgrades with a random chance should register their <i>lowest</i> and <i>highest</i> possible values
     * to the {@link CoinProductionState} via {@link CoinProductionState#registerHighestPossibleWorkingValue(long)}
     * and {@link CoinProductionState#registerLowestPossibleWorkingValue(long)} respectively.
     * <p>
     * An upgrade which does <b>NOT</b> have a random chance will automatically register working or base
     * production increases as the highest and lowest possible increases to the {@link CoinProductionState}. However,
     * lowest and highest possible production increases can still be registered manually <b>AFTER</b> the upgrade to production is applied.
     * 
     * @return True if the upgrade has a random chance, false otherwise
     */
    public abstract boolean hasRandomChance();

}
