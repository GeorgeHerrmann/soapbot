package com.georgster.coinfactory.model;

import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

/**
 * A context of data for a {@link CoinFactory}.
 */
public final class CoinFactoryContext {
    private final String memberId; // Snowflake id of the user that owns this factory

    private final List<FactoryUpgrade> upgrades; // List of upgrades this factory has purchased

    private long investedCoins; // The current production value of the factory (aka the number of coins invested)

    private int prestige; // The number of times the factory has been prestiged

    /**
     * Creates a new, default {@link CoinFactoryContext} for a member with the given Snowflake id.
     * <p>
     * This factory will have 1 invested coin, no upgrades and a prestige level of 0.
     * 
     * @param memberId The Snowflake id of the member who owns the CoinFactory.
     */
    public CoinFactoryContext(String memberId) {
        this.memberId = memberId;
        this.upgrades = new ArrayList<>(); // Empty list of upgrades
        this.investedCoins = 1; // No coins invested
        this.prestige = 0; // No prestige
    }

    /**
     * Creates a new {@link CoinFactoryContext} given a member's Snowflake id, their upgrades, invested coins and prestige level.
     * 
     * @param memberId The member's Snowflake id.
     * @param upgrades The upgrades owned by the member's {@link CoinFactory}.
     * @param investedCoins The amount of invested coins in the member's {@link CoinFactory}.
     * @param prestige The prestige level of the member's {@link CoinFactory}.
     */
    public CoinFactoryContext(String memberId, List<FactoryUpgrade> upgrades, long investedCoins, int prestige) {
        this.memberId = memberId;
        this.upgrades = upgrades;
        this.investedCoins = investedCoins;
        this.prestige = prestige;
    }

    /**
     * Returns the member's Snowflake id associated with this context.
     * 
     * @return The member's Snowflake id associated with this context.
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * Returns a List of {@link FactoryUpgrade FactoryUpgrades} owned by the factory of this context.
     * 
     * @return the {@link FactoryUpgrade FactoryUpgrades} owned by the factory of this context.
     */
    public List<FactoryUpgrade> getUpgrades() {
        return upgrades;
    }

    /**
     * Adds a {@link FactoryUpgrade} to the factory of this context.
     * 
     * @param upgrade The {@link FactoryUpgrade} to add to the factory of this context.
     */
    public void addUpgrade(FactoryUpgrade upgrade) {
        this.upgrades.add(upgrade);
    }

    /**
     * Removes a {@link FactoryUpgrade} from the factory of this context.
     * 
     * @param upgrade The {@link FactoryUpgrade} to remove from the factory of this context.
     * @throws IllegalArgumentException if the upgrade is not owned by the factory of this context.
     */
    public void removeUpgrade(FactoryUpgrade upgrade) {
        this.upgrades.remove(upgrade);
    }

    /**
     * Clears all {@link FactoryUgrade FactoryUpgrades} from the factory of this context.
     */
    public void clearUpgrades() {
        this.upgrades.clear();
    }

    /**
     * Returns the amount of invested coins in the factory of this context.
     * 
     * @return The amount of invested coins in the factory of this context.
     */
    public long getInvestedCoins() {
        return investedCoins;
    }

    /**
     * Sets the amount of invested coins in the factory of this context.
     * 
     * @param investedCoins The amount of invested coins in the factory of this context.
     * @throws IllegalArgumentException if the amount of invested coins is less than 0.
     */
    public void setInvestedCoins(long investedCoins) {
        this.investedCoins = investedCoins;
    }

    /**
     * Adds the given amount of coins to the invested coins in the factory of this context.
     * 
     * @param coins The amount of coins to add to the invested coins in the factory of this context.
     */
    public void addInvestedCoins(long coins) {
        this.investedCoins += coins;
    }

    /**
     * Removes the given amount of coins from the invested coins in the factory of this context.
     * 
     * @param coins The amount of coins to remove from the invested coins in the factory of this context.
     */
    public void removeInvestedCoins(long coins) {
        this.investedCoins -= coins;
        // Ensure that the invested coins do not go below 0
        this.investedCoins = Math.max(this.investedCoins, 0);
    }

    /**
     * Returns the prestige level of the factory of this context.
     * 
     * @return The prestige level of the factory of this context.
     */
    public int getPrestige() {
        return prestige;
    }

    /**
     * Sets the prestige level of the factory of this context.
     * 
     * @param prestige The prestige level of the factory of this context.
     */
    public void setPrestige(int prestige) {
        this.prestige = prestige;
    }

    /**
     * Increments the prestige level of the factory of this context by 1.
     */
    public void addPrestige() {
        this.prestige++;
    }
}
