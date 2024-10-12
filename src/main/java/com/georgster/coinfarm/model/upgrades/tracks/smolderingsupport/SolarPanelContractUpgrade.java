package com.georgster.coinfarm.model.upgrades.tracks.smolderingsupport;

import com.georgster.coinfarm.model.upgrades.CoinProductionState;
import com.georgster.coinfarm.model.upgrades.FactoryUpgrade;

public final class SolarPanelContractUpgrade extends FactoryUpgrade {
        
        public SolarPanelContractUpgrade() {
            super("Solar Panel Contract",
                "Smoldering Support",
                "Sign a contract that puts Solar Panels on your factory, allowing for even more coin production. x1.25 coins to base production each production cycle.",
                2, 750);
        }
    
        public void applyUpgrade(CoinProductionState state) {
            state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 1.25));
        }
}
