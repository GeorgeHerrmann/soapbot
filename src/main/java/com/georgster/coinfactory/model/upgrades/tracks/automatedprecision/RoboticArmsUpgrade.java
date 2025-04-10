package com.georgster.coinfactory.model.upgrades.tracks.automatedprecision;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class RoboticArmsUpgrade extends FactoryUpgrade {
    
    public RoboticArmsUpgrade() {
        super("Robotic Arms",
            "Automated Precision",
            "Install robotic arms to do all the heavy lifting for your factory workers. Less complaining from the workers, more coins for you. Grants +20 coins to base production each cycle.",
            1, 600);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProduction(20);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
