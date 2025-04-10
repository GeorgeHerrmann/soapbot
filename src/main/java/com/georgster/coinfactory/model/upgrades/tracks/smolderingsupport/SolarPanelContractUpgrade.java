package com.georgster.coinfactory.model.upgrades.tracks.smolderingsupport;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class SolarPanelContractUpgrade extends FactoryUpgrade {
        
    public SolarPanelContractUpgrade() {
        super("Solar Panel Contract",
            "Smoldering Support",
            "Sign a contract that puts Solar Panels on your factory, allowing for even more coin production. +75 coins to base production each production cycle.",
            2, 1250);
    }
    
    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProduction(75);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
}
