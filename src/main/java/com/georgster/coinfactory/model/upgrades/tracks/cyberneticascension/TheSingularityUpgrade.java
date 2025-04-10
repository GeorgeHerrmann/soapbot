package com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class TheSingularityUpgrade extends FactoryUpgrade {

    public TheSingularityUpgrade() {
        super("The Singularity",
            "Cybernetic Ascension",
            "The machines have become smarter than their creators. Adds +5000 to base production and increases working production by x1.5.",
            4, 120000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProduction(5000);
        state.upgradeWorkingProduction(0.5);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
