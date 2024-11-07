package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class LiquidCooledComputersUpgrade extends FactoryUpgrade {
    
    public LiquidCooledComputersUpgrade() {
        super("Liquid Cooled Computers",
            "Icey Intervention",
            "Upgrade all your factory's computers to be liquid cooled, allowing much greater processing power for all other upgrades. x1.05 coins to base production and +50 coins to working production for each other upgrade.",
            2, 4000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.05));
        state.upgradeWorkingProductionValue(50 * (state.getUpgradeCount() - 1));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
    
}
