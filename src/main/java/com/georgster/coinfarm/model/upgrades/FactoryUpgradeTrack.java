package com.georgster.coinfarm.model.upgrades;

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
        this.upgrades = List.of(upgrades);
        //sort the list by level
        this.upgrades.sort((u1, u2) -> Integer.compare(u1.getLevel(), u2.getLevel()));
    }

    public String getName() {
        return name;
    }

    public List<FactoryUpgrade> getUpgrades() {
        return upgrades;
    }
}
