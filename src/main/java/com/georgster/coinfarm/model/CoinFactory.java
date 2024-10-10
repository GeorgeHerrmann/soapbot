package com.georgster.coinfarm.model;

import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfarm.model.upgrades.CoinProductionState;
import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;
import com.georgster.coinfarm.model.upgrades.FactoryUpgradeTrack;
import com.georgster.coinfarm.model.upgrades.FactoryUpgradeTracks;
import com.georgster.control.manager.Manageable;
import com.georgster.economy.CoinBank;
import com.georgster.economy.exception.InsufficientCoinsException;

/**
 * A {@link Manageable} that represents a factory that produces coins.
 * <p>
 * This {@link Manageable} is identified by the member's Snowflake id.
 * <p>
 * The {@link CoinFactory} will produce coins based on its current {@link FactoryUpgrade FactoryUpgrades} and {@link #getCurrentProductionValue() production value} each production cycle.
 * <p>
 * {@link FactoryUpgrade FactoryUpgrades} can be purchased and applied to the factory to increase its production rate and will be processed in the order maintained in the {@link #getUpgrades() upgrades} list.
 */
public final class CoinFactory implements Manageable {
    private final String memberId; // Snowflake id of the user that owns this factory

    private final List<FactoryUpgrade> upgrades; // List of upgrades this factory has purchased

    private long currentProductionValue; // The current production value of the factory (aka the number of coins invested)

    /**
     * Constructs a new {@link CoinFactory} with the given member id.
     * 
     * @param memberId the Snowflake id of the user that owns this factory.
     */
    public CoinFactory(String memberId) {
        this.memberId = memberId;
        this.upgrades = new ArrayList<>();
        this.currentProductionValue = 1;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This {@link Manageable Manageble's} identifier the user's member id.
     */
    public String getIdentifier() {
        return memberId;
    }

    /**
     * Processes the coin production of the factory based on its current {@link FactoryUpgrade FactoryUpgrades}.
     * <p>
     * This method will apply all upgrades to the current {@link #getCurrentProductionValue() production value} and return the new {@link CoinProductionState}.
     * 
     * @return the new {@link CoinProductionState} of the factory after applying all upgrades.
     */
    public CoinProductionState process() {
        CoinProductionState state = new CoinProductionState(currentProductionValue);
        upgrades.forEach(upgrade -> upgrade.applyUpgrade(state));
        currentProductionValue = state.getWorkingProductionValue();
        return state;
    }

    /**
     * Returns how many coins this factory produces each production cycle based on its current {@link FactoryUpgrade FactoryUpgrades} and {@link #getCurrentProductionValue() production value}.
     * 
     * @return how many coins this factory produces each production cycle.
     */
    public long getProductionRateValue() {
        CoinProductionState state = new CoinProductionState(currentProductionValue);
        upgrades.forEach(upgrade -> upgrade.applyUpgrade(state));
        return state.finishProductionCycle();
    }

    /**
     * Returns the current production value of the factory (aka the number of coins invested).
     * 
     * @return the current production value of the factory.
     */
    public long getCurrentProductionValue() {
        return currentProductionValue;
    }

    /**
     * Returns all {@link FactoryUpgrade FactoryUpgrades} owned by the factory.
     * 
     * @return all {@link FactoryUpgrade FactoryUpgrades} owned by the factory.
     */
    public List<FactoryUpgrade> getUpgrades() {
        return new ArrayList<>(upgrades);
    }

    /**
     * Returns the first {@link FactoryUpgrade} with the given name owned by the factory.
     * 
     * @param upgradeName the name of the upgrade to get.
     * @return the first {@link FactoryUpgrade} with the given name owned by the factory.
     * @throws IllegalArgumentException if no upgrade with the given name is owned by the factory.
     */
    public FactoryUpgrade getUpgrade(String upgradeName) throws IllegalArgumentException {
        return upgrades.stream()
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
        return upgrades.stream()
                .filter(u -> u.getName().equalsIgnoreCase(upgradeName) && u.getTrackName().equalsIgnoreCase(rewardTrackName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in factory."));
    }

    /**
     * Purchases the {@link FactoryUpgrade} using the factory's {@link #getCurrentProductionValue() invested coins} with the given name and reward track name.
     * 
     * @param rewardTrackName the name of the reward track the upgrade is in.
     * @param upgradeName the name of the upgrade to purchase.
     * @throws IllegalArgumentException if the upgrade with the given name and reward track name does not exist.
     * @throws InsufficientCoinsException if the factory does not have enough coins to purchase the upgrade.
     */
    public void purchaseUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException, InsufficientCoinsException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        if (currentProductionValue < upgrade.getCost()) {
            throw new InsufficientCoinsException("Cannot purchase upgrade " + upgrade.getName() + " with cost " + upgrade.getCost() + " when the factory has only produced " + currentProductionValue + " coins.");
        } else {
            currentProductionValue -= upgrade.getCost();
            upgrade.markAsOwned();
            upgrades.add(upgrade);
        }
    }

    /**
     * Purchases the {@link FactoryUpgrade} using the factory's {@link #getCurrentProductionValue() invested coins}.
     * 
     * @param upgrade the upgrade to purchase.
     * @throws InsufficientCoinsException if the factory does not have enough coins to purchase the upgrade.
     */
    public void purchaseUpgrade(FactoryUpgrade upgrade) throws InsufficientCoinsException {
        if (currentProductionValue < upgrade.getCost()) {
            throw new InsufficientCoinsException("Cannot purchase upgrade " + upgrade.getName() + " with cost " + upgrade.getCost() + " when the factory has only produced " + currentProductionValue + " coins.");
        } else {
            currentProductionValue -= upgrade.getCost();
            upgrade.markAsOwned();
            upgrades.add(upgrade);
        }
    }

    /**
     * Refunds the {@link FactoryUpgrade} with the given name owned by the factory into the factory's {@link #getCurrentProductionValue() invested coins}.
     * 
     * @param rewardTrackName the name of the reward track the upgrade is in.
     * @param upgradeName the name of the upgrade to refund.
     * @throws IllegalArgumentException if the upgrade with the given name is not owned by the factory or one does not exist.
     */
    public void refundUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        upgrade.markAsUnowned();
        if (upgrades.removeIf(u -> u.getName().equals(upgrade.getName()))) {
            currentProductionValue += upgrade.getRefundValue();
        } else {
            throw new IllegalArgumentException("Cannot refund upgrade " + upgrade.getName() + " because it is not owned by the factory.");
        }
    }

    /**
     * Refunds the {@link FactoryUpgrade} owned by the factory into the factory's {@link #getCurrentProductionValue() invested coins}.
     * 
     * @param upgrade the upgrade to refund.
     * @throws IllegalArgumentException if the upgrade is not owned by the factory.
     */
    public void refundUpgrade(FactoryUpgrade upgrade) throws IllegalArgumentException {
        upgrade.markAsUnowned();
        if (upgrades.removeIf(u -> u.getName().equals(upgrade.getName()))) {
            currentProductionValue += upgrade.getRefundValue();
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
        return upgrades.stream().anyMatch(u -> u.getName().equals(upgrade.getName()));
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
        return upgrades.stream().anyMatch(u -> u.getName().equals(upgrade.getName()));
    }

    /**
     * Returns all available {@link FactoryUpgradeTrack FactoryUpgradeTracks} with their {@link FactoryUpgrade FactoryUpgrades} marked as owned if the factory has them.
     * 
     * @return all available {@link FactoryUpgradeTrack FactoryUpgradeTracks} with their {@link FactoryUpgrade FactoryUpgrades} marked as owned if the factory has them.
     */
    public List<FactoryUpgradeTrack> getCurrentUpgradeTracks() {
        List<FactoryUpgradeTrack> currentUpgradeTracks = new ArrayList<>(FactoryUpgradeTracks.getAvailableUpgradeTracks());
        currentUpgradeTracks.forEach(track -> {
            track.getUpgrades().forEach(upgrade -> {
                if (hasUpgrade(upgrade)) {
                    upgrade.markAsOwned();
                }
            });
        });
        return currentUpgradeTracks;
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
        } else if (currentProductionValue < amount) {
            throw new InsufficientCoinsException("Cannot withdrawl more coins than the factory has produced.");
        } else {
            currentProductionValue -= amount;
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
            currentProductionValue += amount;
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
        if (newSpot < 0 || newSpot >= upgrades.size()) {
            throw new IllegalArgumentException("Cannot swap upgrade to spot " + newSpot + " because it is out of bounds.");
        } else {
            int oldSpot = upgrades.indexOf(upgrade);
            upgrades.set(oldSpot, upgrades.get(newSpot));
            upgrades.set(newSpot, upgrade);
        }
    }
}
