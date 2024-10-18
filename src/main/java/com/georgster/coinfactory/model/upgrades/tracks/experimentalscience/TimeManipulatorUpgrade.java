package com.georgster.coinfactory.model.upgrades.tracks.experimentalscience;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class TimeManipulatorUpgrade extends FactoryUpgrade {
    
    public TimeManipulatorUpgrade() {
        super("Time Manipulator",
            "Experimental Science",
            "Why wait for time to pass naturally when you can manipulate it to your advantage? Though remember, this science is experimental - results may vary. Multiplies base production by *1.7 or adds +75 to working production.",
            3, 50000);
    }

    public void applyUpgrade(CoinProductionState state) {
        if (Math.random() < 0.5) {
            state.upgradeBaseProductionValue((long) (state.getBaseProductionValue() * 0.7));
        } else {
            state.upgradeWorkingProductionValue(75);
        }
    }

}
