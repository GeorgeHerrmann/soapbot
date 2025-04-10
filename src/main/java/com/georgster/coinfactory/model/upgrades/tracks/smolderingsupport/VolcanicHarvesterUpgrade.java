package com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.experimentalscience.CloningFacilityUpgrade;

public final class VolcanicHarvesterUpgrade extends FactoryUpgrade {
    
    public VolcanicHarvesterUpgrade() {
        super("Volcanic Harvester",
            "Smoldering Support",
            "Hire a crew of scientists and engineers who will harvest the energy from a volcano to allow for greater coin production. Adds +200 coins to starting production and x1.6 coins to working production each cycle." +
            " If the Cloning Facility Upgrade is owned, your scientists will clone themselves to work even faster, increasing the working production upgrade to x2.1.",
            3, 150000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeStartingProduction(200);
        if (state.getUpgrades().stream().anyMatch(CloningFacilityUpgrade.class::isInstance)) {
            state.upgradeWorkingProduction(1.1);
        } else {
            state.upgradeWorkingProduction(0.6);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
    
}
