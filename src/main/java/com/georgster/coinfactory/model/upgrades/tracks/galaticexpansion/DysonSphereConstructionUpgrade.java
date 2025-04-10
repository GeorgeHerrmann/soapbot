package com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.automatedprecision.QuantumComputingIntegrationUpgrade;

public final class DysonSphereConstructionUpgrade extends FactoryUpgrade {
    
    public DysonSphereConstructionUpgrade() {
        super("Dyson Sphere Construction",
            "Galactic Expansion",
            " By building a Dyson Sphere around a star, you’ve unlocked the greatest energy source imaginable! Just... dont accidentally melt the solar system. Increases working production by x1.5 and adds +200 to base production each cycle with a 5% chance to erase half of your coins unless the Quantum Computing Integration Upgrade is owned.",
            3, 75000);
    }

    public void applyUpgrade(CoinProductionState state) {
        if (state.getUpgrades().stream().noneMatch(QuantumComputingIntegrationUpgrade.class::isInstance)) {
            state.registerPossibleCoinWipe(0.5);
            if (Math.random() < 0.05) {
                state.wipeCoins(0.5);
            }
        }

        state.upgradeWorkingProduction(0.5);
        state.upgradeBaseProduction(200);

        state.registerHighestPossibleWorkingValue(200 + ((long) (state.getHighestPossibleWorkingValue() * 0.5)));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }
}
