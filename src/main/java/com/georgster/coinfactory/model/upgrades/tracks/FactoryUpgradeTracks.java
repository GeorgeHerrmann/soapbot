package com.georgster.coinfactory.model.upgrades.tracks;

import java.util.List;

import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.automatedprecision.AiPoweredAssemblyLineUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.automatedprecision.QuantumComputingIntegrationUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.automatedprecision.RoboticArmsUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension.AugmentedWorkersUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension.FullyAutomatedWorkforceUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension.NeuralNetworkCoordinationUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension.TheSingularityUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.experimentalscience.AntimatterReactorUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.experimentalscience.CloningFacilityUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.experimentalscience.TimeManipulatorUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.financialshenanigans.CorporateMergerUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.financialshenanigans.StockMarketManipulationUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.financialshenanigans.TaxHavenUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion.AsteroidMiningUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion.DysonSphereConstructionUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion.MoonMiningColonyUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.iceyintervention.GlaciarMovementHarvesterUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.iceyintervention.LiquidCooledComputersUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.iceyintervention.WarmthProducingJoggersUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.occultengineering.DemonPoweredFurnaceUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.occultengineering.NecromancersWorkshopUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.occultengineering.RealityBendingPortalUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.occultengineering.SummoningCircleOfFortuneUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport.HeatEnergyConverterUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport.SolarPanelContractUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport.VolcanicHarvesterUpgrade;

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
            new FactoryUpgradeTrack("Smoldering Support", "Warming our future one coin at a time", new HeatEnergyConverterUpgrade(), new SolarPanelContractUpgrade(), new VolcanicHarvesterUpgrade()),
            new FactoryUpgradeTrack("Icey Intervention", "Would an ice age be profitable?", new WarmthProducingJoggersUpgrade(), new LiquidCooledComputersUpgrade(), new GlaciarMovementHarvesterUpgrade()),
            new FactoryUpgradeTrack("Automated Precision", "A high-tech approach to maximize efficiency through automation.", new RoboticArmsUpgrade(), new AiPoweredAssemblyLineUpgrade(), new QuantumComputingIntegrationUpgrade()),
            new FactoryUpgradeTrack("Galactic Expansion", "Take your coin production to the moon. Literally.", new MoonMiningColonyUpgrade(), new AsteroidMiningUpgrade(), new DysonSphereConstructionUpgrade()),
            new FactoryUpgradeTrack("Experimental Science", "Delve into cutting-edge, questionably legal science for big profits.", new AntimatterReactorUpgrade(), new CloningFacilityUpgrade(), new TimeManipulatorUpgrade()),
            new FactoryUpgradeTrack("Financial Shenanigans", "It's called 'creative accounting'", new TaxHavenUpgrade(), new CorporateMergerUpgrade(), new StockMarketManipulationUpgrade()),
            new FactoryUpgradeTrack("Occult Engineering", "The supernatural is actually quite profitable.", new DemonPoweredFurnaceUpgrade(), new NecromancersWorkshopUpgrade(), new RealityBendingPortalUpgrade(), new SummoningCircleOfFortuneUpgrade()),
            new FactoryUpgradeTrack("Cybernetic Ascension", "When humans aren’t enough, it’s time to upgrade... the humans.", new AugmentedWorkersUpgrade(), new NeuralNetworkCoordinationUpgrade(), new FullyAutomatedWorkforceUpgrade(), new TheSingularityUpgrade())
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
