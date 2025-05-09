package com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class NeuralNetworkCoordinationUpgrade extends FactoryUpgrade {
    
    public NeuralNetworkCoordinationUpgrade() {
        super("Neural Network Coordination",
            "Cybernetic Ascension",
            "Sync the minds of your workers through a neural network, coordinating production with perfect efficiency. Increases working production by 1.3x each cycle.",
            2, 5000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProduction(0.3);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
