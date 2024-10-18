package com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class AsteroidMiningUpgrade extends FactoryUpgrade {

    public AsteroidMiningUpgrade() {
        super("Asteroid Mining",
            "Galactic Expansion",
            "Sending miners into space to extract asteroid resources may sound risky, but it’s a small price to pay for a huge payday. Multiplies base production by *1.4 each cycle.",
            2, 8000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.4));
    }
    
}
