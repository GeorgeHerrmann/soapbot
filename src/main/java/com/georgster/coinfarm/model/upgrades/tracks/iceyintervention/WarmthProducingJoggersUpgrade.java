package com.georgster.coinfarm.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfarm.model.upgrades.CoinProductionState;
import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;

public final class WarmthProducingJoggersUpgrade extends FactoryUpgrade {
    
    public WarmthProducingJoggersUpgrade() {
        super("Warmth Producing Joggers",
            "Icey Intervention",
            "When your factory gets cold, crew members will now jog to produce heat, allowing them to work faster. *1.2 coins to base production and +5 coins each production cycle.",
            1, 350);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 1.2));
        state.upgradeWorkingProductionValue(5);
    }

}
