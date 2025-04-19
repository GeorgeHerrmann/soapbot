package com.georgster.coinfactory.model.upgrades.tracks.financialshenanigans;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.SoapNumbers;

public final class StockMarketManipulationUpgrade extends FactoryUpgrade {
    
    public StockMarketManipulationUpgrade() {
        super("Stock Market Manipulation",
            "Financial Shenanigans",
            "Use insider info to manipulate the stock market for massive gains, just don't let the authorities catch-wind - they might shut you down. Increases working production by 1.5x and adds +100 to base production each cycle, but there's a 5% chance the upgrade will be disabled for one cycle.",
            3, 65000);
    }

    public void applyUpgrade(CoinProductionState state) {
        if (SoapNumbers.getRandomDouble(0, 1) < 0.05) {
            state.upgradeWorkingProduction(0.5);
            state.upgradeBaseProduction(100);
        }

        /* If the upgrade is disabled, no coins are produced. So the lowest possible working increase is zero, therefore does not need to be registered */
        state.registerHighestPossibleWorkingValue(100 + ((long) (state.getBaseProductionValue() * 0.5)));
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
