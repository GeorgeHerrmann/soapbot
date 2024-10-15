package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class GlaciarMovementHarvesterUpgrade extends FactoryUpgrade {
    
    public GlaciarMovementHarvesterUpgrade() {
        super("Glaciar Movement Harvester",
            "Icey Intervention",
            "Creates energy converters near ice glaciars that will move and create large amounts of energy, allowing for varying amounts of gains. Multiplies base production value by a random value between 1.1-2.5x.",
            3, 6250);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * (1.1 + Math.random() * 1.4)));
    }

}
