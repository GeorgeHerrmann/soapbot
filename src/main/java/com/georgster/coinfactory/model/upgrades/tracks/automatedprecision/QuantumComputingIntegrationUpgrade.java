package com.georgster.coinfactory.model.upgrades.tracks.automatedprecision;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class QuantumComputingIntegrationUpgrade extends FactoryUpgrade {
    
    public QuantumComputingIntegrationUpgrade() {
        super("Quantum Computing Integration",
            "Automated Precision",
            "By leveraging quantum computing, your factory performs calculations faster than ever. Increases base production by 15%, adds +100 to working production each cycle, and DOUBLES starting production if any level 4 upgrade is owned.",
            3, 15000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.15));
        state.upgradeWorkingProductionValue(100);
        if (state.getUpgrades().stream().anyMatch(upgrade -> upgrade.getLevel() == 4)) {
            state.upgradeStartingProductionValue(state.getStartingProductionValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
