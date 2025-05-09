package com.georgster.coinfactory.model.upgrades.tracks.financialshenanigans;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class CorporateMergerUpgrade extends FactoryUpgrade {
    
    public CorporateMergerUpgrade() {
        super("Corporate Merger",
            "Financial Shenanigans",
            "Merge with a competitor to gain access to new production methods and streamline operations. Increases working production by 1.1x per 3 upgrades you own.",
            2, 12000);
    }

    public void applyUpgrade(CoinProductionState state) {
        int numApplicationTimes = state.getUpgrades().size() / 3;

        for (int i = 0; i < numApplicationTimes; i++) {
            state.upgradeWorkingProduction(0.1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }

}
