package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class GlaciarMovementHarvesterUpgrade extends FactoryUpgrade {
    
    public GlaciarMovementHarvesterUpgrade() {
        super("Glaciar Movement Harvester",
            "Icey Intervention",
            "Creates energy converters near ice glaciars that will move and create large amounts of energy. Increases working production by 1.9x",
            3, 12500);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProduction(0.9);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
