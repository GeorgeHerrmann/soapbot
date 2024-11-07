package com.georgster.coinfactory.model.upgrades.tracks.cyberneticascension;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class FullyAutomatedWorkforceUpgrade extends FactoryUpgrade {
    
    public FullyAutomatedWorkforceUpgrade() {
        super("Fully Automated Workforce",
            "Cybernetic Ascension",
            "Replace your entire workforce with robots. They don’t take breaks, but your other upgrades will need some time to adjust. Each upgrade owned multiplies base production by x1.1, but has a 1% chance to fail and wipe all produced coins in the cycle.",
            3, 50000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.registerPossibleCoinPercentageWipe(1);

        for (int i = 0; i < state.getUpgradeCount(); i++) {
            state.registerHighestPossibleWorkingValue((long) (state.getHighestPossibleWorkingValue() * 0.1));
            if (Math.random() < 0.01) {
                state.wipeCoinsPercentage(1);
                break;
            } else {
                state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.1));
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
