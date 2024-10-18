package com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class HeatEnergyConverterUpgrade extends FactoryUpgrade {
    
    public HeatEnergyConverterUpgrade() {
        super("Heat Energy Converter",
            "Smoldering Support",
            "Heat produced by your CoinFactory gets reused to make even more coins. +15 to working production each production cycle.",
            1, 300);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProductionValue(15);
    }

}
