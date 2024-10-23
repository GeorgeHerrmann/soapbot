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
        for (int i = 0; i < state.getUpgrades().size(); i++) {
            if (Math.random() < 0.01) {
                state.wipeCoins(state.getWorkingProductionValue());
                break;
            } else {
                state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.1));
            }
        }
    }

}
