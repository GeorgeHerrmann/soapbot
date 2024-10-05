package com.georgster.coinfarm.model.upgrades;

import java.util.ArrayList;
import java.util.List;

public final class FactoryUpgradeTrack {
    private final String name;
    private final List<FactoryUpgrade> upgrades;

    public FactoryUpgradeTrack(String name, List<FactoryUpgrade> upgrades) {
        this.name = name;
        this.upgrades = upgrades;
        //sort the list by level
        this.upgrades.sort((u1, u2) -> Integer.compare(u1.getLevel(), u2.getLevel()));
    }

    public FactoryUpgradeTrack(String name, FactoryUpgrade... upgrades) {
        this.name = name;
        this.upgrades = new ArrayList<>(List.of(upgrades));
        //sort the list by level
        this.upgrades.sort((u1, u2) -> Integer.compare(u1.getLevel(), u2.getLevel()));
    }

    public String getName() {
        return name;
    }

    public List<FactoryUpgrade> getUpgrades() {
        return upgrades;
    }

    public boolean isMaxUpgrade(String upgradeName) {
        if (upgradeName.equals("No Upgrade")) {
            return false;
        }

        return isMaxUpgrade(getUpgrade(upgradeName));
    }

    public boolean isLowestUpgrade(String upgradeName) {
        if (upgradeName.equals("No Upgrade")) {
            return false;
        }
        return upgrades.indexOf(getUpgrade(upgradeName)) == 0;
    }

    public boolean ownsAny() {
        return upgrades.stream().anyMatch(FactoryUpgrade::isOwned);
    }

    public FactoryUpgrade getUpgrade(String upgradeName) throws IllegalArgumentException {
        if (upgradeName.equals("No Upgrade")) {
            return new AbsentFactoryUpgrade();
        }

        return upgrades.stream()
                .filter(upgrade -> upgrade.getName().equalsIgnoreCase(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in track " + name));
    }

    public FactoryUpgrade getUpgrade(int level) throws IllegalArgumentException {
        return upgrades.stream()
                .filter(upgrade -> upgrade.getLevel() == level)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with level " + level + " in track " + name));
    }

    public FactoryUpgrade getNextUpgrade(FactoryUpgrade upgrade) {
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

    public FactoryUpgrade getPreviousUpgrade(FactoryUpgrade upgrade) {
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

    public FactoryUpgrade getCurrentUpgrade() {
        // return the last upgrade that is owned (i.e the highest level upgrade that is owned)
        return upgrades.stream()
                .filter(FactoryUpgrade::isOwned)
                .reduce((first, second) -> second)
                .orElse(new AbsentFactoryUpgrade());
    }

    private boolean isMaxUpgrade(FactoryUpgrade upgrade) {
        return upgrades.indexOf(upgrade) == upgrades.size() - 1;
    }
}
