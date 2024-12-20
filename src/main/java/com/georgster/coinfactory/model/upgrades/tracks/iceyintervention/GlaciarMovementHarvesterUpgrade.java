package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class GlaciarMovementHarvesterUpgrade extends FactoryUpgrade {
    
    public GlaciarMovementHarvesterUpgrade() {
        super("Glaciar Movement Harvester",
            "Icey Intervention",
            "Creates energy converters near ice glaciars that will move and create large amounts of energy, allowing for varying amounts of gains. Multiplies base production value by a random value between 1.1-2.5x.",
            3, 12500);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * (0.1 + Math.random() * 0.4)));

        // Register x1.1 as the lowest possible value for the working production value and x2.5 as the highest possible value
        state.registerLowestPossibleWorkingValue((long) (state.getLowestPossibleWorkingValue() * 0.1));
        state.registerHighestPossibleWorkingValue((long) (state.getHighestPossibleWorkingValue() * 1.5));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
