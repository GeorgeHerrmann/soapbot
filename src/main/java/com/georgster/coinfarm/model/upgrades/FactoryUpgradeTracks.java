package com.georgster.coinfarm.model.upgrades;

import java.util.List;

public final class FactoryUpgradeTracks {

    private FactoryUpgradeTracks() {
        throw new IllegalStateException("Utility class");
    }

    public static List<FactoryUpgradeTrack> getAvailableUpgradeTracks() {
        return List.of(
            new FactoryUpgradeTrack("Working", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade()),
            new FactoryUpgradeTrack("Production", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade()),
            new FactoryUpgradeTrack("Cost", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade()),
            new FactoryUpgradeTrack("Speed", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade()),
            new FactoryUpgradeTrack("Efficiency", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade()),
            new FactoryUpgradeTrack("Quality", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade()),
            new FactoryUpgradeTrack("Safety", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade())
        );
    }

    public static FactoryUpgradeTrack getUpgradeTrack(String name) throws IllegalArgumentException {
        return getAvailableUpgradeTracks().stream()
                .filter(track -> track.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade track with name " + name));
    }

    public static FactoryUpgrade getUpgrade(String trackName, String upgradeName) throws IllegalArgumentException {
        return getUpgradeTrack(trackName).getUpgrades().stream()
                .filter(upgrade -> upgrade.getName().equalsIgnoreCase(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in track " + trackName));
    }
}
