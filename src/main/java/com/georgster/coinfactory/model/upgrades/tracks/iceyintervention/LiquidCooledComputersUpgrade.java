package com.georgster.coinfactory.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class LiquidCooledComputersUpgrade extends FactoryUpgrade {
    
    public LiquidCooledComputersUpgrade() {
        super("Liquid Cooled Computers",
            "Icey Intervention",
            "Upgrade all your factory's computers to be liquid cooled, allowing much greater processing power for all other upgrades. Increases working production by x1.05 and adds +50 coins to working production for each other upgrade.",
            2, 4000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProduction(0.05);
        state.upgradeBaseProduction((long) 50 * (state.getUpgradeCount() - 1));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
    
}
