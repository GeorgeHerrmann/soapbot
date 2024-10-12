package com.georgster.coinfarm.model.upgrades.tracks.iceyintervention;

import com.georgster.coinfarm.model.upgrades.CoinProductionState;
import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;

public final class LiquidCooledComputersUpgrade extends FactoryUpgrade {
    
    public LiquidCooledComputersUpgrade() {
        super("Liquid Cooled Computers",
            "Icey Intervention",
            "Upgrade all your factory's computers to be liquid cooled, allowing much greater processing power for all other upgrades. *1.05 coins to base production and +50 coins to working production for each other upgrade.",
            2, 1000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 1.05));
        state.upgradeWorkingProductionValue(50 * state.getUpgrades().size());
    }
    
}
