package com.georgster.coinfactory.model.upgrades.tracks.occultengineering;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.SoapNumbers;

public final class NecromancersWorkshopUpgrade extends FactoryUpgrade {
    
    public NecromancersWorkshopUpgrade() {
        super("Necromancer's Workshop",
            "Occult Engineering",
            "Reanimate the \"disappeared\" workers to maximize productivity and properly work the Demon-Powered Furnace. Each upgrade has a 10% chance to bring back a deceased worker, adding 400 coins to base production each cycle. The Demon-Powered Furnace upgrade no longer wipes coins.",
            2, 8000);
    }

    public void applyUpgrade(CoinProductionState state) {
        for (int i = 0; i < state.getUpgrades().size(); i++) {
            if (SoapNumbers.getRandomDouble(0, 1) < 0.1) {
                state.upgradeBaseProduction(400);
            }
        }

        state.registerHighestPossibleBaseValue(state.getUpgradeCount() * 400);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
