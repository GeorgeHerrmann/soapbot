package com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.SoapNumbers;

public final class FullyAutomatedWorkforceUpgrade extends FactoryUpgrade {
    
    public FullyAutomatedWorkforceUpgrade() {
        super("Fully Automated Workforce",
            "Cybernetic Ascension",
            "Replace your entire workforce with robots. They don’t take breaks, but your other upgrades will need some time to adjust. Each upgrade owned increases working production by x1.1, but has a 1% chance to fail and wipe all produced coins in the cycle.",
            3, 50000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.registerPossibleCoinWipe(1);

        for (int i = 0; i < state.getUpgradeCount(); i++) {
            state.registerHighestPossibleWorkingValue((long) (state.getBaseProductionValue() * 0.1));
            if (SoapNumbers.getRandomDouble(0, 1) < 0.01) {
                state.wipeCoins(1);
                break;
            } else {
                state.upgradeWorkingProduction(0.1);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
