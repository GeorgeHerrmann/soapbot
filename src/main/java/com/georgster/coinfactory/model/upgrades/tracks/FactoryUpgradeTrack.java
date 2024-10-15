package com.georgster.coinfactory.model.upgrades.tracks;

import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfactory.model.upgrades.AbsentFactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

/**
 * A track of {@link FactoryUpgrade FactoryUpgrades} ordered by their {@link FactoryUpgrade#getLevel() level}.
 * <p>
 * A {@link FactoryUpgradeTrack} should not have multiple {@link FactoryUpgrade FactoryUpgrades} with the same level and/or name.
 */
public final class FactoryUpgradeTrack {
    private final String name; // The name of the track
    private final List<FactoryUpgrade> upgrades; // The upgrades in the track

    /**
     * Constructs a new FactoryUpgradeTrack with the given name and upgrades.
     * 
     * @param name The name of the track
     * @param upgrades The upgrades in the track
     */
    public FactoryUpgradeTrack(String name, List<FactoryUpgrade> upgrades) {
        this.name = name;
        this.upgrades = upgrades;
        //sort the list by level
        this.upgrades.sort((u1, u2) -> Integer.compare(u1.getLevel(), u2.getLevel()));
    }

    /**
     * Constructs a new FactoryUpgradeTrack with the given name and upgrades.
     * 
     * @param name The name of the track
     * @param upgrades The upgrades in the track
     */
    public FactoryUpgradeTrack(String name, FactoryUpgrade... upgrades) {
        this.name = name;
        this.upgrades = new ArrayList<>(List.of(upgrades));
        //sort the list by level
        this.upgrades.sort((u1, u2) -> Integer.compare(u1.getLevel(), u2.getLevel()));
    }

    /**
     * Returns the name of the track.
     * 
     * @return The name of the track
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link FactoryUpgrade upgrades} in the track.
     * 
     * @return The {@link FactoryUpgrade upgrades} in the track
     */
    public List<FactoryUpgrade> getUpgrades() {
        return upgrades;
    }

    /**
     * Returns whether the {@link FactoryUpgrade} with the given {@code upgradeName} is the highest level upgrade in the track.
     * 
     * @param upgradeName The name of the upgrade
     * @return {@code true} if the {@link FactoryUpgrade} with the given {@code upgradeName} is the highest level upgrade in the track, {@code false} otherwise
     */
    public boolean isMaxUpgrade(String upgradeName) {
        if (upgradeName.equals("No Upgrade")) {
            return false;
        }

        return isMaxUpgrade(getUpgrade(upgradeName));
    }

    /**
     * Returns whether the {@link FactoryUpgrade} with the given {@code upgradeName} is the lowest level upgrade in the track.
     * 
     * @param upgradeName The name of the upgrade
     * @return {@code true} if the {@link FactoryUpgrade} with the given {@code upgradeName} is the lowest level upgrade in the track, {@code false} otherwise
     */
    public boolean isLowestUpgrade(String upgradeName) {
        if (upgradeName.equals("No Upgrade")) {
            return false;
        }
        return upgrades.indexOf(getUpgrade(upgradeName)) == 0;
    }

    /**
     * Returns if any {@link FactoryUpgrade upgrades} in the track marked as owned.
     * 
     * @return {@code true} if any {@link FactoryUpgrade upgrades} in the track are marked as owned, {@code false} otherwise
     */
    public boolean ownsAny() {
        return upgrades.stream().anyMatch(FactoryUpgrade::isOwned);
    }

    /**
     * Returns the first {@link FactoryUpgrade} with the given {@code upgradeName}.
     * 
     * @param upgradeName The name of the upgrade
     * @return The first {@link FactoryUpgrade} with the given {@code upgradeName}
     * @throws IllegalArgumentException If no {@link FactoryUpgrade} with the given {@code upgradeName} is in the track
     */
    public FactoryUpgrade getUpgrade(String upgradeName) throws IllegalArgumentException {
        if (upgradeName.equals("No Upgrade")) {
            return new AbsentFactoryUpgrade();
        }

        return upgrades.stream()
                .filter(upgrade -> upgrade.getName().equalsIgnoreCase(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in track " + name));
    }

    /**
     * Returns the first {@link FactoryUpgrade} with the given {@code level}.
     * 
     * @param level The level of the upgrade
     * @return The first {@link FactoryUpgrade} with the given {@code level}
     * @throws IllegalArgumentException If no {@link FactoryUpgrade} with the given {@code level} is in the track
     */
    public FactoryUpgrade getUpgrade(int level) throws IllegalArgumentException {
        return upgrades.stream()
                .filter(upgrade -> upgrade.getLevel() == level)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with level " + level + " in track " + name));
    }

    /**
     * Returns the {@link FactoryUpgrade} with the next level after the given {@code upgrade}, or
     * an {@link AbsentFactoryUpgrade} if the given {@code upgrade} is the highest level upgrade in the track.
     * 
     * @param upgrade The {@link FactoryUpgrade} to get the next upgrade for
     * @return The {@link FactoryUpgrade} with the next level after the given {@code upgrade}, or an {@link AbsentFactoryUpgrade} if the given {@code upgrade} is the highest level upgrade in the track
     * @throws IllegalArgumentException If the given {@code upgrade} is not part of this track
     */
    public FactoryUpgrade getNextUpgrade(FactoryUpgrade upgrade) throws IllegalArgumentException {
        if (upgrade instanceof AbsentFactoryUpgrade) {
            return upgrades.get(0);
        }
        int index = upgrades.indexOf(getUpgrade(upgrade.getName()));
        if (index == -1) {
            throw new IllegalArgumentException("Upgrade " + upgrade.getName() + " is not part of this track");
        } else if (index == upgrades.size() - 1) {
            return new AbsentFactoryUpgrade();
        } else {
            return upgrades.get(index + 1);
        }
    }

    /**
     * Returns the {@link FactoryUpgrade} with the previous level before the given {@code upgrade}, or
     * an {@link AbsentFactoryUpgrade} if the given {@code upgrade} is the lowest level upgrade in the track.
     * 
     * @param upgrade The {@link FactoryUpgrade} to get the previous upgrade for
     * @return The {@link FactoryUpgrade} with the previous level before the given {@code upgrade}, or an {@link AbsentFactoryUpgrade} if the given {@code upgrade} is the lowest level upgrade in the track
     * @throws IllegalArgumentException If the given {@code upgrade} is not part of this track
     */
    public FactoryUpgrade getPreviousUpgrade(FactoryUpgrade upgrade) throws IllegalArgumentException {
        if (upgrade instanceof AbsentFactoryUpgrade) {
            return new AbsentFactoryUpgrade();
        }
        int index = upgrades.indexOf(getUpgrade(upgrade.getName()));
        if (index == -1) {
            throw new IllegalArgumentException("Upgrade " + upgrade.getName() + " is not part of this track");
        } else if (index == 0) {
            return new AbsentFactoryUpgrade();
        } else {
            return upgrades.get(index - 1);
        }
    }

    /**
     * Returns the {@link FactoryUpgrade} that is currently owned in the track, or
     * an {@link AbsentFactoryUpgrade} if no upgrades are owned.
     * 
     * @return The {@link FactoryUpgrade} that is currently owned in the track, or an {@link AbsentFactoryUpgrade} if no upgrades are owned
     */
    public FactoryUpgrade getCurrentUpgrade() {
        // return the last upgrade that is owned (i.e the highest level upgrade that is owned)
        return upgrades.stream()
                .filter(FactoryUpgrade::isOwned)
                .reduce((first, second) -> second)
                .orElse(new AbsentFactoryUpgrade());
    }

    /**
     * Returns if the given {@code upgrade} is the highest level upgrade in the track.
     * 
     * @param upgrade The {@link FactoryUpgrade} to check
     * @return {@code true} if the given {@code upgrade} is the highest level upgrade in the track, {@code false} otherwise
     */
    private boolean isMaxUpgrade(FactoryUpgrade upgrade) {
        return upgrades.indexOf(upgrade) == upgrades.size() - 1;
    }
}
