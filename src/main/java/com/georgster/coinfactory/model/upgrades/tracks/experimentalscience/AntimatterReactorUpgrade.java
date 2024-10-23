package com.georgster.coinfactory.model.upgrades.tracks.experimentalscience;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class AntimatterReactorUpgrade extends FactoryUpgrade {
    
    public AntimatterReactorUpgrade() {
        super("Antimatter Reactor",
            "Experimental Science",
            "With an antimatter reactor powering your factory, the coin output is astronomical. Multiplies starting production by 1.25x each cycle.",
            1, 5000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeStartingProductionValue((long) (state.getStartingProductionValue() * 0.25));
    }

}
