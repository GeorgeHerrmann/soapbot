package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class WarmthProducingJoggersUpgrade extends FactoryUpgrade {
    
    public WarmthProducingJoggersUpgrade() {
        super("Warmth Producing Joggers",
            "Icey Intervention",
            "When your factory gets cold, crew members will now jog to produce heat, allowing them to work faster. Increases working production by x1.15 and adds +15 coins to starting production each production cycle.",
            1, 850);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProduction(0.15);
        state.upgradeStartingProduction(15);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
