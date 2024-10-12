package com.georgster.coinfarm.model.upgrades.tracks.smolderingsupport;

import com.georgster.coinfarm.model.upgrades.CoinProductionState;
import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;

public final class HeatEnergyConverterUpgrade extends FactoryUpgrade {
    
    public HeatEnergyConverterUpgrade() {
        super("Heat Energy Converter",
            "Smoldering Support",
            "Heat produced by your CoinFactory gets reused to make even more coins. +15 to working production each production cycle.",
            1, 200);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProductionValue(15);
    }

}
