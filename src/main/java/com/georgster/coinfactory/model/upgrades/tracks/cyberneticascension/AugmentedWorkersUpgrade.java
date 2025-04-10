package com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class AugmentedWorkersUpgrade extends FactoryUpgrade {
    
    public AugmentedWorkersUpgrade() {
        super("Augmented Workers",
            "Cybernetic Ascension",
            "Enhance your workers with cybernetic implants for better productivity. Adds +80 to base production each cycle.",
            1, 600);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProduction(80);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
