package com.georgster.coinfactory.model.upgrades.tracks.automatedprecision;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class QuantumComputingIntegrationUpgrade extends FactoryUpgrade {
    
    public QuantumComputingIntegrationUpgrade() {
        super("Quantum Computing Integration",
            "Automated Precision",
            "By leveraging quantum computing, your factory performs calculations faster than ever. Multiplies base production by *1.15 and adds +100 to working production each cycle.",
            3, 15000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.15));
        state.upgradeWorkingProductionValue(100);
    }

}
