package com.georgster.coinfactory.model.upgrades.tracks.experimentalscience;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.SoapNumbers;

public final class TimeManipulatorUpgrade extends FactoryUpgrade {
    
    public TimeManipulatorUpgrade() {
        super("Time Manipulator",
            "Experimental Science",
            "Why wait for time to pass naturally when you can manipulate it to your advantage? Though remember, this science is experimental - results may vary. Increases working production by 1.7x or adds +75 to base production and adds +100 to starting production.",
            3, 50000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeStartingProduction(100);

        state.registerHighestPossibleWorkingValue(0.7);
        state.registerHighestPossibleBaseValue(75);

        if (SoapNumbers.getRandomDouble(0, 1) < 0.5) {
            state.upgradeWorkingProduction(0.7);
        } else {
            state.upgradeBaseProduction(75);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
