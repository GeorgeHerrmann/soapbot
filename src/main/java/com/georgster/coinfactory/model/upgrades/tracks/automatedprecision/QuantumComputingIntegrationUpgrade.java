package com.georgster.coinfactory.model.upgrades.tracks.automatedprecision;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class QuantumComputingIntegrationUpgrade extends FactoryUpgrade {
    
    public QuantumComputingIntegrationUpgrade() {
        super("Quantum Computing Integration",
            "Automated Precision",
            "By leveraging quantum computing, your factory performs calculations faster than ever. Adds +100 to base production, increases working production by 15%, and DOUBLES starting production if any level 4 upgrade is owned.",
            3, 15000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProduction(100);
        state.upgradeWorkingProduction(0.15);
        if (state.getUpgrades().stream().anyMatch(upgrade -> upgrade.getLevel() == 4)) {
            state.upgradeStartingProduction(state.getStartingProductionValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
