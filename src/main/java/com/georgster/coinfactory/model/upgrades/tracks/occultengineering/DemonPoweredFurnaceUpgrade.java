package com.georgster.coinfactory.model.upgrades.tracks.occultengineering;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;

public final class DemonPoweredFurnaceUpgrade extends FactoryUpgrade {

    public DemonPoweredFurnaceUpgrade() {
        super("Demon-Powered Furnace",
            "Occult Engineering",
            "A dark pact fuels your factory with demonic energy, costing you a small price each cycle. The Demon and Necromancers Workers Union (DNWU) takes safety fines if no proper workshop is present. Adds +250 to base production and +40 to starting production but wipes 20% of ALL produced coins each cycle unless the Necromancer's Workshop upgrade is owned.",
            1, 4000);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProduction(250);
        state.upgradeStartingProduction(40);
        if (state.getUpgrades().stream().noneMatch(NecromancersWorkshopUpgrade.class::isInstance)) {
            state.wipeCoins(0.2);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return false;
    }
    
}
