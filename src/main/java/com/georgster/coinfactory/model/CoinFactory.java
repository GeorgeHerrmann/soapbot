package com.georgster.coinfactory.model;

import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.FactoryUpgradeTrack;
import com.georgster.coinfactory.model.upgrades.tracks.FactoryUpgradeTracks;
import com.georgster.control.manager.Manageable;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.economy.CoinBank;
import com.georgster.economy.exception.InsufficientCoinsException;
import com.georgster.settings.TimezoneOption;
import com.georgster.settings.UserSettings;
import com.georgster.util.DateTimed;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

/**
 * A {@link Manageable} that represents a factory that produces coins.
 * <p>
 * This {@link Manageable} is identified by the member's Snowflake id.
 * <p>
 * The {@link CoinFactory} will produce coins based on its current {@link FactoryUpgrade FactoryUpgrades} and {@link #getinvestedCoins() production value} each production cycle.
 * <p>
 * {@link FactoryUpgrade FactoryUpgrades} can be purchased and applied to the factory to increase its production rate and will be processed in the order maintained in the {@link #getUpgrades() upgrades} list.
 */
public final class CoinFactory implements Manageable {
    private final CoinFactoryContext context; // The context of the factory

    /**
     * Constructs a new {@link CoinFactory} with the given member id.
     * 
     * @param memberId the Snowflake id of the user that owns this factory.
     */
    public CoinFactory(String memberId) {
        this.context = new CoinFactoryContext(memberId);
    }

    /**
     * Constructs a new {@link CoinFactory} with the given member id, upgrades, invested coins, and prestige level.
     * 
     * @param memberId the Snowflake id of the user that owns this factory.
     * @param upgrades the list of {@link FactoryUpgrade FactoryUpgrades} owned by the factory.
     * @param investedCoins the current production value of the factory (aka the number of coins invested).
     * @param prestige the number of times the factory has been prestiged.
     */
    public CoinFactory(String memberId, List<FactoryUpgrade> upgrades, long investedCoins, int prestige) {
        this.context = new CoinFactoryContext(memberId, upgrades, investedCoins, prestige);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This {@link Manageable Manageble's} identifier the user's member id.
     */
    public String getIdentifier() {
        return context.getMemberId();
    }

    /**
     * Processes the coin production of the factory based on its current {@link FactoryUpgrade FactoryUpgrades}.
     * <p>
     * This method will apply all upgrades to the current {@link #getinvestedCoins() production value} and return the new {@link CoinProductionState}.
     * 
     * @return the new {@link CoinProductionState} of the factory after applying all upgrades.
     */
    public CoinProductionState process() {
        CoinProductionState state = new CoinProductionState(context, true);
        state.processUpgrades();
        context.addInvestedCoins(state.getWorkingProductionValue());
        return state;
    }

    /**
     * Simulates the coin production of the factory based on its current {@link FactoryUpgrade FactoryUpgrades}
     * and returns the resulting {@link CoinProductionState} without modifying the factory's {@link #getinvestedCoins() invested coins}.
     * 
     * @return the resulting {@link CoinProductionState} of the factory after applying all upgrades.
     */
    public CoinProductionState simulateProductionCycle() {
        CoinProductionState state = new CoinProductionState(context, false);
        state.processUpgrades();
        return state;
    }

    /**
     * Returns how many coins this factory produces each production cycle based on its current {@link FactoryUpgrade FactoryUpgrades} and {@link #getinvestedCoins() production value}
     * based on a {@link #simulateProductionCycle() simulated production cycle}.
     * 
     * @return how many coins this factory produces each production cycle.
     */
    public long getProductionRateValue() {
        return simulateProductionCycle().getWorkingProductionValue();
    }

    /**
     * Returns the current production value of the factory (aka the number of coins invested).
     * 
     * @return the current production value of the factory.
     */
    public long getInvestedCoins() {
        return context.getInvestedCoins();
    }

    /**
     * Returns all {@link FactoryUpgrade FactoryUpgrades} owned by the factory.
     * 
     * @return all {@link FactoryUpgrade FactoryUpgrades} owned by the factory.
     */
    public List<FactoryUpgrade> getUpgrades() {
        return new ArrayList<>(context.getUpgrades());
    }

    /**
     * Returns the first {@link FactoryUpgrade} with the given name owned by the factory.
     * 
     * @param upgradeName the name of the upgrade to get.
     * @return the first {@link FactoryUpgrade} with the given name owned by the factory.
     * @throws IllegalArgumentException if no upgrade with the given name is owned by the factory.
     */
    public FactoryUpgrade getUpgrade(String upgradeName) throws IllegalArgumentException {
        return getUpgrades().stream()
                .filter(u -> u.getName().equalsIgnoreCase(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in factory."));
    }

    /**
     * Returns the first {@link FactoryUpgrade} with the given name and reward track name owned by the factory.
     * 
     * @param rewardTrackName the name of the reward track the upgrade is in.
     * @param upgradeName the name of the upgrade to get.
     * @return the first {@link FactoryUpgrade} with the given name and reward track name owned by the factory.
     * @throws IllegalArgumentException if no upgrade with the given name and reward track name is owned by the factory.
     */
    public FactoryUpgrade getUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException {
        return getUpgrades().stream()
                .filter(u -> u.getName().equalsIgnoreCase(upgradeName) && u.getTrackName().equalsIgnoreCase(rewardTrackName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in factory."));
    }

    /**
     * Purchases the {@link FactoryUpgrade} using the factory's {@link #getinvestedCoins() invested coins} with the given name and reward track name.
     * 
     * @param rewardTrackName the name of the reward track the upgrade is in.
     * @param upgradeName the name of the upgrade to purchase.
     * @throws IllegalArgumentException if the upgrade with the given name and reward track name does not exist.
     * @throws InsufficientCoinsException if the factory does not have enough coins to purchase the upgrade.
     */
    public void purchaseUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException, InsufficientCoinsException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        if (getInvestedCoins() < upgrade.getCost(getPrestige())) {
            throw new InsufficientCoinsException("Cannot purchase upgrade " + upgrade.getName() + " with cost " + upgrade.getCost(getPrestige()) + " when the factory has only produced " + getInvestedCoins() + " coins.");
        } else {
            context.removeInvestedCoins(upgrade.getCost(getPrestige()));
            upgrade.markAsOwned();
            context.addUpgrade(upgrade);
        }
    }

    /**
     * Purchases the {@link FactoryUpgrade} using the factory's {@link #getinvestedCoins() invested coins}.
     * 
     * @param upgrade the upgrade to purchase.
     * @throws InsufficientCoinsException if the factory does not have enough coins to purchase the upgrade.
     */
    public void purchaseUpgrade(FactoryUpgrade upgrade) throws InsufficientCoinsException {
        if (getInvestedCoins() < upgrade.getCost(getPrestige())) {
            throw new InsufficientCoinsException("Cannot purchase upgrade " + upgrade.getName() + " with cost " + upgrade.getCost(getPrestige()) + " when the factory has only produced " + getInvestedCoins() + " coins.");
        } else {
            context.removeInvestedCoins(upgrade.getCost(getPrestige()));
            upgrade.markAsOwned();
            context.addUpgrade(upgrade);
        }
    }

    /**
     * Refunds the {@link FactoryUpgrade} with the given name owned by the factory into the factory's {@link #getinvestedCoins() invested coins}.
     * 
     * @param rewardTrackName the name of the reward track the upgrade is in.
     * @param upgradeName the name of the upgrade to refund.
     * @throws IllegalArgumentException if the upgrade with the given name is not owned by the factory or one does not exist.
     */
    public void refundUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        upgrade.markAsUnowned();
        if (getUpgrades().stream().anyMatch(u -> u.getName().equals(upgrade.getName()))) {
            context.removeUpgrade(upgrade);
            context.addInvestedCoins(upgrade.getRefundValue(getPrestige()));
        } else {
            throw new IllegalArgumentException("Cannot refund upgrade " + upgrade.getName() + " because it is not owned by the factory.");
        }
    }

    /**
     * Refunds the {@link FactoryUpgrade} owned by the factory into the factory's {@link #getinvestedCoins() invested coins}.
     * 
     * @param upgrade the upgrade to refund.
     * @throws IllegalArgumentException if the upgrade is not owned by the factory.
     */
    public void refundUpgrade(FactoryUpgrade upgrade) throws IllegalArgumentException {
        upgrade.markAsUnowned();
        if (getUpgrades().stream().anyMatch(u -> u.getName().equals(upgrade.getName()))) {
            context.removeUpgrade(upgrade);
            context.addInvestedCoins(upgrade.getRefundValue(getPrestige()));
        } else {
            throw new IllegalArgumentException("Cannot refund upgrade " + upgrade.getName() + " because it is not owned by the factory.");
        }
    }

    /**
     * Returns whether the factory has the {@link FactoryUpgrade} with the given name.
     * 
     * @param upgradeName the name of the upgrade to check for.
     * @return whether the factory has the upgrade with the given name.
     */
    public boolean hasUpgrade(FactoryUpgrade upgrade) {
        return getUpgrades().stream().anyMatch(u -> u.getName().equals(upgrade.getName()));
    }

    /**
     * Returns whether the factory has the {@link FactoryUpgrade} with the given name and reward track name.
     * 
     * @param rewardTrackName the name of the reward track the upgrade is in.
     * @param upgradeName the name of the upgrade to check for.
     * @return whether the factory has the upgrade with the given name and reward track name.
     * @throws IllegalArgumentException if the upgrade with the given name and reward track name does not exist.
     */
    public boolean hasUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        return getUpgrades().stream().anyMatch(u -> u.getName().equals(upgrade.getName()));
    }

    /**
     * Returns all available {@link FactoryUpgradeTrack FactoryUpgradeTracks} with their {@link FactoryUpgrade FactoryUpgrades} marked as owned if the factory has them.
     * 
     * @return all available {@link FactoryUpgradeTrack FactoryUpgradeTracks} with their {@link FactoryUpgrade FactoryUpgrades} marked as owned if the factory has them.
     */
    public List<FactoryUpgradeTrack> getCurrentUpgradeTracks() {
        List<FactoryUpgradeTrack> currentUpgradeTracks = new ArrayList<>(FactoryUpgradeTracks.getAvailableUpgradeTracks());
        currentUpgradeTracks.forEach(track -> 
            track.getUpgrades().forEach(upgrade -> {
                if (hasUpgrade(upgrade)) {
                    upgrade.markAsOwned();
                }
            })
        );
        return currentUpgradeTracks;
    }

    /**
     * Returns the number of upgrades owned by the factory.
     * 
     * @return the number of upgrades owned by the factory.
     */
    public int getUpgradeCount() {
        return getUpgrades().size();
    }

    /**
     * Returns the number of upgrades owned by the factory at the given level.
     * 
     * @param level the level to get the upgrade count for.
     * @return the number of upgrades owned by the factory at the given level.
     * @throws IllegalArgumentException if the level is non-positive (zero or negative integer).
     */
    public int getUpgradeCountByLevel(int level) throws IllegalArgumentException {
        if (level <= 0) {
            throw new IllegalArgumentException("Cannot get upgrade count by level for a non-positive level.");
        } else {
            return (int) getUpgrades().stream().filter(u -> u.getLevel() == level).count();
        }
    }

    /**
     * Withdraws the given amount of coins from the factory and deposits them into the given {@link CoinBank}.
     * 
     * @param amount the amount of coins to withdraw.
     * @param bank the {@link CoinBank} to deposit the coins into.
     * @throws IllegalArgumentException if the amount is negative.
     * @throws InsufficientCoinsException if the factory does not have enough coins to withdraw.
     */
    public void withdraw(long amount, CoinBank bank) throws IllegalArgumentException, InsufficientCoinsException {
        if (amount < 1) {
            throw new IllegalArgumentException("Cannot withdrawl a negative amount of coins.");
        } else if (context.getInvestedCoins() < amount) {
            throw new InsufficientCoinsException("Cannot withdrawl more coins than the factory has produced.");
        } else {
            context.removeInvestedCoins(amount);
            bank.deposit(amount);
        }
    }

    /**
     * Deposits the given amount of coins from the given {@link CoinBank} into the factory.
     * 
     * @param amount the amount of coins to deposit.
     * @param bank the {@link CoinBank} to withdraw the coins from.
     * @throws IllegalArgumentException if the amount is negative.
     * @throws InsufficientCoinsException if the {@link CoinBank} does not have enough coins to deposit.
     */
    public void deposit(long amount, CoinBank bank) throws IllegalArgumentException, InsufficientCoinsException {
        if (amount > 0) {
            bank.withdrawl(amount);
            context.addInvestedCoins(amount);
        } else {
            throw new IllegalArgumentException("Cannot deposit a negative amount of coins.");
        }
    }

    /**
     * Swaps the given {@link FactoryUpgrade} with the upgrade at the given spot in the {@link #getUpgrades() upgrades} list.
     * 
     * @param upgrade the upgrade to swap.
     * @param newSpot the spot in the {@link #getUpgrades() upgrades} list to swap the upgrade to.
     * @throws IllegalArgumentException if the new spot is out of bounds.
     */
    public void swap (FactoryUpgrade upgrade, int newSpot) throws IllegalArgumentException {
        context.swap(upgrade, newSpot);
    }

    /**
     * Prestiges this {@link CoinFactory} by resetting all owned upgrades, taking the prestige cost from the {@link #getinvestedCoins() invested coins}, and incrementing the prestige level.
     * <p>
     * <i>The cost of prestige is the sum of the refund value of all upgrades from {@link #getCurrentUpgradeTracks()} multiplied by the prestige level plus one.
     * Prestiging the factory requires all available {@link FactoryUpgrade FactoryUpgrades} to be owned.</i>
     * 
     * @throws InsufficientCoinsException if the factory does not have enough coins to prestige.
     * @throws IllegalStateException if the factory does not own all available upgrades.
     * @see {@link #getCurrentUpgradeTracks()} for all available upgrades that must be owned to prestige.
     */
    public void prestige() throws InsufficientCoinsException, IllegalStateException {
        boolean canBePrestiged = canBePrestiged();
        boolean ownsAllUpgrades = ownsAllUpgrades();
        long prestigeCost = getPrestigeCost();
        long investedCoins = getInvestedCoins();

        if (!canBePrestiged && !ownsAllUpgrades) {
            throw new IllegalStateException("Cannot prestige the factory when the factory does not own all upgrades.");
        }

        if (!canBePrestiged && investedCoins < prestigeCost) {
            throw new InsufficientCoinsException("Cannot prestige the factory when the factory has only produced " + investedCoins + " coins and the cost of prestige is " + prestigeCost + " coins.");
        }

        context.clearUpgrades(); // Clear all upgrades
        context.removeInvestedCoins(prestigeCost);
        context.addPrestige(); // Increment prestige level
    }

    /**
     * Returns whether the factory can be prestiged.
     * 
     * @return True if the factory can be prestiged, false otherwise.
     */
    public boolean canBePrestiged() {
        if (getInvestedCoins() < getPrestigeCost()) {
            return false;
        }

        if (!ownsAllUpgrades()) {
            return false;
        }

        return true;
    }

    /**
     * Returns the cost of prestige for the factory.
     * 
     * @return the cost of prestige for the factory.
     */
    public long getPrestigeCost() {
        int prestige = getPrestige();
        return (prestige + 1) * (getCurrentUpgradeTracks().stream().flatMap(track -> track.getUpgrades().stream()).mapToLong(upgrade -> upgrade.getRefundValue(prestige)).sum());
    }

    /**
     * Returns whether the factory owns all available upgrades.
     * 
     * @return True if the factory owns all available upgrades, false otherwise.
     */
    public boolean ownsAllUpgrades() {
        return getCurrentUpgradeTracks().stream().allMatch(track -> track.getUpgrades().stream().allMatch(up -> up.isOwned()));
    }

    /**
     * Returns the number of times the factory has been prestiged.
     * If the factory has not been prestiged, 0 is returned.
     * 
     * @return the number of times the factory has been prestiged.
     */
    public int getPrestige() {
        return context.getPrestige();
    }

    /**
     * Returns the {@link Color} associated with the given prestige level.
     * 
     * @param prestigeLevel the prestige level to get the color for.
     * @return the {@link Color} associated with the given prestige level.
     */
    public static Color getPrestigeColor(int prestigeLevel) {
        if (prestigeLevel == 0) {
            return Color.PINK;
        } else if (prestigeLevel < 2) {
            return Color.RUBY;
        } else if (prestigeLevel < 6) {
            return Color.GREEN;
        } else if (prestigeLevel < 10) {
            return Color.TAHITI_GOLD;
        } else {
            return Color.MAGENTA;
        }
    }

    /**
     * Returns the {@link Color} associated with this factory's current prestige level.
     * 
     * @return the {@link Color} associated with this factory's current prestige level.
     */
    public Color getPrestigeColor() {
        return CoinFactory.getPrestigeColor(getPrestige());
    }

    /**
     * Returns an {@link EmbedCreateSpec} with the details of this {@link CoinFactory}.
     * 
     * @param manager The {@link UserProfileManager} that manages the UserProfile for this {@link CoinFactory}.
     * @param userSettings The {@link UserSettings} of the UserProfile for this {@link CoinFactory} <i>(used for time display)</i>.
     * @return An {@link EmbedCreateSpec} with the details of this {@link CoinFactory}.
     */
    public EmbedCreateSpec getDetailEmbed(UserProfileManager manager, UserSettings userSettings) {
        CoinProductionState simulatedState = simulateProductionCycle();

        String factoryOwner = new GuildInteractionHandler(manager.getGuild()).getMemberById(getIdentifier()).getUsername();

        StringBuilder description = new StringBuilder();
        description.append("***PRESTIGE: " + getPrestige() + "***\n\n");
        description.append("**Invested Coins:** *" + getInvestedCoins() + " coins*\n");
        description.append("**Production Rate:** *" + simulatedState.getLowestPossibleWorkingValue() + " - " + simulatedState.getHighestPossibleWorkingValue() + " coins per cycle*\n");
        DateTimed nextProcessTime = manager.getNextFactoryProcessTime();
        description.append("This Factory will process coins next at *" + nextProcessTime.getFormattedTime(userSettings) + " " + TimezoneOption.getSettingDisplay(userSettings.getTimezoneSetting()) + "* on *" + nextProcessTime.getFormattedDate(userSettings) + "*.\n\n");
        description.append("**Number of Upgrades:** *" + getUpgradeCount() + "*\n");
        description.append("- **Level 1 Upgrades:** *" + getUpgradeCountByLevel(1) + "*\n");
        description.append("- **Level 2 Upgrades:** *" + getUpgradeCountByLevel(2) + "*\n");
        description.append("- **Level 3 Upgrades:** *" + getUpgradeCountByLevel(3) + "*\n");
        description.append("- **Level 4 Upgrades:** *" + getUpgradeCountByLevel(4) + "*\n\n");

        if (canBePrestiged()) {
            description.append("*This factory can be prestiged for* **" + getPrestigeCost() + " coins.**");
        } else {
            description.append("*This factory cannot be prestiged.*");
        }

        return EmbedCreateSpec.builder()
            .title(factoryOwner + "'s Coin Factory")
            .description(description.toString())
            .color(getPrestigeColor())
            .build();
    }
}
