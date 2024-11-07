package com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class AsteroidMiningUpgrade extends FactoryUpgrade {

    public AsteroidMiningUpgrade() {
        super("Asteroid Mining",
            "Galactic Expansion",
            "Sending miners into space to extract asteroid resources may sound risky, but it’s a small price to pay for a huge payday. Multiplies base production by 1.25x each cycle and increases starting production by 50 coins.",
            2, 8000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.25));
        state.upgradeStartingProductionValue(50);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
    
}
