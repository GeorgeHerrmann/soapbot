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

public final class CoinFactory implements Manageable {
    private final String memberId;

    private final List<FactoryUpgrade> upgrades;

    private long currentProductionValue;

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

    public long getCurrentProductionValue() {
        return currentProductionValue;
    }

    public List<FactoryUpgrade> getUpgrades() {
        return new ArrayList<>(upgrades);
    }

    public FactoryUpgrade getUpgrade(String upgradeName) {
        return upgrades.stream()
                .filter(u -> u.getName().equalsIgnoreCase(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in factory."));
    }

    public FactoryUpgrade getUpgrade(String rewardTrackName, String upgradeName) {
        return upgrades.stream()
                .filter(u -> u.getName().equalsIgnoreCase(upgradeName) && u.getTrackName().equalsIgnoreCase(rewardTrackName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in factory."));
    }

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

    public void purchaseUpgrade(FactoryUpgrade upgrade) throws InsufficientCoinsException {
        if (currentProductionValue < upgrade.getCost()) {
            throw new InsufficientCoinsException("Cannot purchase upgrade " + upgrade.getName() + " with cost " + upgrade.getCost() + " when the factory has only produced " + currentProductionValue + " coins.");
        } else {
            currentProductionValue -= upgrade.getCost();
            upgrade.markAsOwned();
            upgrades.add(upgrade);
        }
    }

    public void refundUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        upgrade.markAsUnowned();
        if (upgrades.removeIf(u -> u.getName().equals(upgrade.getName()))) {
            currentProductionValue += upgrade.getRefundValue();
        } else {
            throw new IllegalArgumentException("Cannot refund upgrade " + upgrade.getName() + " because it is not owned by the factory.");
        }
    }

    public void refundUpgrade(FactoryUpgrade upgrade) {
        upgrade.markAsUnowned();
        if (upgrades.removeIf(u -> u.getName().equals(upgrade.getName()))) {
            currentProductionValue += upgrade.getRefundValue();
        } else {
            throw new IllegalArgumentException("Cannot refund upgrade " + upgrade.getName() + " because it is not owned by the factory.");
        }
    }

    public boolean hasUpgrade(FactoryUpgrade upgrade) {
        return upgrades.stream().anyMatch(u -> u.getName().equals(upgrade.getName()));
    }

    public boolean hasUpgrade(String rewardTrackName, String upgradeName) throws IllegalArgumentException {
        FactoryUpgrade upgrade = FactoryUpgradeTracks.getUpgrade(rewardTrackName, upgradeName);
        return upgrades.stream().anyMatch(u -> u.getName().equals(upgrade.getName()));
    }

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

    public void deposit(long amount, CoinBank bank) throws IllegalArgumentException, InsufficientCoinsException {
        if (amount > 0) {
            bank.withdrawl(amount);
            currentProductionValue += amount;
        } else {
            throw new IllegalArgumentException("Cannot deposit a negative amount of coins.");
        }
    }

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
