package com.georgster.coinfarm.model.upgrades;

import java.util.List;

public final class FactoryUpgradeTracks {
    public static final List<FactoryUpgradeTrack> AVAILABLE_UPGRADE_TRACKS = List.of(
            new FactoryUpgradeTrack("Working", new AddFiftyWorkingUpgrade(), new DoubleBaseUpgrade())
    );

    private FactoryUpgradeTracks() {
        throw new IllegalStateException("Utility class");
    }

    public static List<FactoryUpgradeTrack> getAvailableUpgradeTracks() {
        return AVAILABLE_UPGRADE_TRACKS;
    }

    public static FactoryUpgradeTrack getUpgradeTrack(String name) throws IllegalArgumentException {
        return AVAILABLE_UPGRADE_TRACKS.stream()
                .filter(track -> track.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade track with name " + name));
    }

    public static FactoryUpgrade getUpgrade(String trackName, String upgradeName) throws IllegalArgumentException {
        return getUpgradeTrack(trackName).getUpgrades().stream()
                .filter(upgrade -> upgrade.getName().equals(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in track " + trackName));
    }
}
