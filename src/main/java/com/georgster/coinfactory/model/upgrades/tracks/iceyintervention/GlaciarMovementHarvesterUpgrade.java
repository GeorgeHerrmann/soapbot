package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.SoapNumbers;

public final class GlaciarMovementHarvesterUpgrade extends FactoryUpgrade {
    
    public GlaciarMovementHarvesterUpgrade() {
        super("Glaciar Movement Harvester",
            "Icey Intervention",
            "Creates energy converters near ice glaciars that will move and create large amounts of energy. Increases working production by a random value between 1.2x and 2.5x each cycle.",
            3, 12500);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.registerLowestPossibleWorkingValue(0.2);
        state.registerHighestPossibleWorkingValue(1.5);
        state.upgradeWorkingProduction(SoapNumbers.getRandomDouble(0.2, 1.5));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
