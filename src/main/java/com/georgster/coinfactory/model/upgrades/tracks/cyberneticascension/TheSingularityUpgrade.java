package com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class TheSingularityUpgrade extends FactoryUpgrade {
    
    public static final boolean IDLING = true;

    public TheSingularityUpgrade() {
        super("The Singularity",
            "Cybernetic Ascension",
            "The machines have become smarter than their creators. Production skyrockets—until the singularity takes over and resets the factory’s operations every so often. Every other cycle this upgrade doubles base production and adds +10000 to working production, with the other cycle being idle as the machines \"reset\".",
            4, 120000);
    }

    public void applyUpgrade(CoinProductionState state) {
        if (!IDLING) {
            state.upgradeBaseProductionValue((long) (state.getBaseProductionValue()));
            state.upgradeWorkingProductionValue(10000);
        }
    }

}
