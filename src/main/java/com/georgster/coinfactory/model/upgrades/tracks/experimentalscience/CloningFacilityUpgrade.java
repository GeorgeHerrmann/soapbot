package com.georgster.coinfactory.model.upgrades.tracks.experimentalscience;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class CloningFacilityUpgrade extends FactoryUpgrade {
    
    public CloningFacilityUpgrade() {
        super("Cloning Facility",
            "Experimental Science",
            "Clone your best workers for maximum efficiency. Don’t think too hard about the ethics... Adds +10 to working production for every level 1 upgrade you have, +50 for every level 2 upgrade, +500 for every level 3 upgrade and +2000 for every level 4 upgrade.",
            2, 18000);
    }

    public void applyUpgrade(CoinProductionState state) {
        long workingProductionValue = 0;
        for (FactoryUpgrade upgrade : state.getUpgrades()) {
            if (upgrade.getLevel() == 1) {
                workingProductionValue += 10;
            } else if (upgrade.getLevel() == 2) {
                workingProductionValue += 50;
            } else if (upgrade.getLevel() == 3) {
                workingProductionValue += 500;
            } else if (upgrade.getLevel() == 4) {
                workingProductionValue += 2000;
            }
        }
        state.upgradeWorkingProductionValue(workingProductionValue);
    }
    
}
