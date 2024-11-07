package com.georgster.coinfactory.model.upgrades.tracks.experimentalscience;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class TimeManipulatorUpgrade extends FactoryUpgrade {
    
    public TimeManipulatorUpgrade() {
        super("Time Manipulator",
            "Experimental Science",
            "Why wait for time to pass naturally when you can manipulate it to your advantage? Though remember, this science is experimental - results may vary. Multiplies base production by 1.7x or adds +75 to working production and adds +100 to starting production.",
            3, 50000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.registerLowestPossibleWorkingValue(75);
        state.registerHighestPossibleWorkingValue((long) (state.getHighestPossibleWorkingValue() * 0.7));

        state.upgradeStartingProductionValue(100);
        if (Math.random() < 0.5) {
            state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.7));
        } else {
            state.upgradeWorkingProductionValue(75);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
