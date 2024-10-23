package com.georgster.coinfactory.model.upgrades.tracks.automatedprecision;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class AiPoweredAssemblyLineUpgrade extends FactoryUpgrade {
    
    public AiPoweredAssemblyLineUpgrade() {
        super("AI-Powered Assembly Line",
            "Automated Precision",
            "Artificial intelligence takes over your assembly line, predicting and avoiding production bottlenecks before they even happen. The future is here, and it’s profitable! Increases starting production value by 30%",
            2, 3500);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeStartingProductionValue((long) (state.getStartingProductionValue() * 0.3));
    }

}
