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
     * Returns the cost of the upgrade.
     * 
     * @return The cost of the upgrade
     */
    public long getCost() {
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
     * Returns the refund value of the upgrade.
     * <p>
     * The floor is returned if the refund value is not a whole number.
     * 
     * @return The refund value of the upgrade.
     */
    public long getRefundValue() {
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

}
