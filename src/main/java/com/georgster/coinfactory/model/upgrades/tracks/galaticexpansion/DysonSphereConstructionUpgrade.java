package com.georgster.coinfactory.model.upgrades.tracks.galaticexpansion;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.automatedprecision.QuantumComputingIntegrationUpgrade;

public final class DysonSphereConstructionUpgrade extends FactoryUpgrade {
    
    public DysonSphereConstructionUpgrade() {
        super("Dyson Sphere Construction",
            "Galactic Expansion",
            " By building a Dyson Sphere around a star, you’ve unlocked the greatest energy source imaginable! Just... dont accidentally melt the solar system. Multiplies base production by *1.7 and adds +200 to working production each cycle with a 5% chance to erase half of your coins unless the Quantum Computing Integration Upgrade is owned.",
            3, 75000);
    }

    public void applyUpgrade(CoinProductionState state) {
        if (state.getUpgrades().stream().noneMatch(upgrade -> upgrade instanceof QuantumComputingIntegrationUpgrade) && Math.random() < 0.05) {
            state.wipeCoins(state.getTotalCoins() / 2);
        } else {
            state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.7));
            state.upgradeWorkingProductionValue(200);
        }
    }
}
