package com.georgster.coinfarm.model.upgrades.tracks;

import java.util.List;

import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;
import com.georgster.coinfarm.model.upgrades.tracks.iceyintervention.GlaciarMovementHarvesterUpgrade;
import com.georgster.coinfarm.model.upgrades.tracks.iceyintervention.LiquidCooledComputersUpgrade;
import com.georgster.coinfarm.model.upgrades.tracks.iceyintervention.WarmthProducingJoggersUpgrade;
import com.georgster.coinfarm.model.upgrades.tracks.smolderingsupport.HeatEnergyConverterUpgrade;
import com.georgster.coinfarm.model.upgrades.tracks.smolderingsupport.SolarPanelContractUpgrade;
import com.georgster.coinfarm.model.upgrades.tracks.smolderingsupport.VolcanicHarvesterUpgrade;

/**
 * Factory class for {@link FactoryUpgradeTrack FactoryUpgradeTracks}.
 * <p>
 * All available {@link FactoryUpgradeTrack FactoryUpgradeTracks} and their respective
 * {@link FactoryUpgrade FactoryUpgrades} are defined via {@link #getAvailableUpgradeTracks()}.
 * <p>
 * All {@link FactoryUpgradeTracks} methods are case insensitive.
 */
public final class FactoryUpgradeTracks {

    /**
     * Utility class.
     */
    private FactoryUpgradeTracks() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates and returns a list of available {@link FactoryUpgradeTrack FactoryUpgradeTracks} with their respective {@link FactoryUpgrade FactoryUpgrades}.
     * <p>
     * The returned list will not have any {@link FactoryUpgrade FactoryUpgrades} marked as owned.
     * 
     * @return A list of available {@link FactoryUpgradeTrack FactoryUpgradeTracks}
     */
    public static List<FactoryUpgradeTrack> getAvailableUpgradeTracks() {
        return List.of(
            new FactoryUpgradeTrack("Smoldering Support", new HeatEnergyConverterUpgrade(), new SolarPanelContractUpgrade(), new VolcanicHarvesterUpgrade()),
            new FactoryUpgradeTrack("Icey Intervention", new WarmthProducingJoggersUpgrade(), new LiquidCooledComputersUpgrade(), new GlaciarMovementHarvesterUpgrade())
        );
    }

    /**
     * Returns the first {@link FactoryUpgradeTrack} with the given name.
     * 
     * @param name The name of the track
     * @return The {@link FactoryUpgradeTrack} with the given name
     * @throws IllegalArgumentException If no {@link FactoryUpgradeTrack} with the given name exists
     */
    public static FactoryUpgradeTrack getUpgradeTrack(String name) throws IllegalArgumentException {
        return getAvailableUpgradeTracks().stream()
                .filter(track -> track.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade track with name " + name));
    }

    /**
     * Returns the first {@link FactoryUpgrade} with the given name in the {@link FactoryUpgradeTrack} with the given name.
     * 
     * @param trackName The name of the track
     * @param upgradeName The name of the upgrade
     * @return The {@link FactoryUpgrade} with the given name in the {@link FactoryUpgradeTrack} with the given name
     * @throws IllegalArgumentException If no {@link FactoryUpgrade} with the given name exists in the {@link FactoryUpgradeTrack} with the given name
     */
    public static FactoryUpgrade getUpgrade(String trackName, String upgradeName) throws IllegalArgumentException {
        return getUpgradeTrack(trackName).getUpgrades().stream()
                .filter(upgrade -> upgrade.getName().equalsIgnoreCase(upgradeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No upgrade with name " + upgradeName + " in track " + trackName));
    }
}
