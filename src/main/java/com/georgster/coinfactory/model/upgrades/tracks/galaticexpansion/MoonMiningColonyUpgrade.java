package com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class MoonMiningColonyUpgrade extends FactoryUpgrade {

    public MoonMiningColonyUpgrade() {
        super("Moon Mining Colony",
            "Galactic Expansion",
            "Your lunar colony is hard at work mining resources. Sure, there’s no air, but that’s their problem—not yours. Grants +40 to working production each cycle.",
            1, 1500);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProductionValue(40);
    }
    
}
