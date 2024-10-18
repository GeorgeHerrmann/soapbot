package com.georgster.coinfactory.model.upgrades.tracks.occultengineering;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class SummoningCircleOfFortuneUpgrade extends FactoryUpgrade {
    
    public SummoningCircleOfFortuneUpgrade() {
        super("Summoning Circle of Fortune",
            "Occult Engineering",
            "Summon spirits to channel vast amounts of fortune into your factory, acquiring whatever you end up pulling in. Has an equal chance to add either 75,000 coins to working production, x2 to base production, or do nothing.",
            4, 275000);
    }

    public void applyUpgrade(CoinProductionState state) {
        int random = (int) (Math.random() * 3);
        if (random == 0) {
            state.upgradeWorkingProductionValue(75000);
        } else if (random == 1) {
            state.upgradeBaseProductionValue(state.getBaseProductionValue());
        }
    }
    
}
