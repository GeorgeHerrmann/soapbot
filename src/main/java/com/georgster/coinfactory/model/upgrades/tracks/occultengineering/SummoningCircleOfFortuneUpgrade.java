package com.georgster.coinfactory.model.upgrades.tracks.occultengineering;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.SoapNumbers;

public final class SummoningCircleOfFortuneUpgrade extends FactoryUpgrade {
    
    public SummoningCircleOfFortuneUpgrade() {
        super("Summoning Circle of Fortune",
            "Occult Engineering",
            "Summon spirits to channel vast amounts of fortune into your factory, acquiring whatever you end up pulling in. Has an equal chance to add either 50,000 coins to base production, increase working production by x2, or do nothing.",
            4, 275000);
    }

    public void applyUpgrade(CoinProductionState state) {
        long possibleWorkingUpgrade = state.getBaseProductionValue() + 50000;
        long possibleBaseUpgrade = state.getBaseProductionValue() * 2;

        if (possibleWorkingUpgrade > possibleBaseUpgrade) { // If working upgrade is higher than base upgrade, set highest possible working increase to 75000, otherwise set it to base upgrade increase
            state.registerHighestPossibleWorkingValue(50000);
        } else {
            state.registerHighestPossibleWorkingValue(state.getBaseProductionValue());
        }

        int random = (int) (SoapNumbers.getRandomDouble(0, 1) * 3);
        if (random == 0) {
            state.upgradeBaseProduction(50000);
        } else if (random == 1) {
            state.upgradeWorkingProduction(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }
    
}
