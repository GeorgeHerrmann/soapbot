package com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class VolcanicHarvesterUpgrade extends FactoryUpgrade {
    
    public VolcanicHarvesterUpgrade() {
        super("Volcanic Harvester",
            "Smoldering Support",
            "Hire a crew of scientists and engineers who will harvest the energy from a volcano to allow for greater coin production. x2 coins to base production and +50 coins to working production each production cycle.",
            3, 150000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue(state.getBaseProductionValue());
        state.upgradeWorkingProductionValue(50);
    }
    
}
