package com.georgster.coinfactory.model.upgrades.tracks.financialshenanigans;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class TaxHavenUpgrade extends FactoryUpgrade {

    public TaxHavenUpgrade() {
        super("Tax Haven",
            "Financial Shenanigans",
            "Relocate your factory’s headquarters to a convenient tax haven - no more unnecessary 'payments'. All upgrades are exempt from some taxes and produce 3% more coins.",
            1, 2000);
    }

    public void applyUpgrade(CoinProductionState state) {
        for (int i = 0; i < state.getUpgradeCount(); i++) {
            state.upgradeWorkingProduction(0.03);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
    
}
