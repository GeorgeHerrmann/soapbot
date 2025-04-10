package com.georgster.coinfactory.model.upgrades.tracks.occultengineering;

import java.time.LocalDateTime;

import com.georgster.coinfactory.model.upgrades.CoinProductionState;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.util.DateTimed;

public final class RealityBendingPortalUpgrade extends FactoryUpgrade {
    
    public RealityBendingPortalUpgrade() {
        super("Reality Bending Portal",
            "Occult Engineering",
            "Tap into an unknown dimension for vast amounts of energy. The longer the portal remains open, the higher the risk of reality collapsing around you. If successful on a random chance, this upgrade will x3 Working Production. Earlier in the day, this upgrade has a greater chance of success, with diminishing chances as the day progresses.",
            3, 45000);
    }

    public void applyUpgrade(CoinProductionState state) {
        LocalDateTime now = DateTimed.getCurrentLocalDateTime();

        // give a 10% chance at 1am, decreasing by 0.4% every hour
        double chance = 0.1 - (now.getHour() * 0.004);

        if (Math.random() < chance) {
            state.upgradeWorkingProduction(2.0);
        }

        state.registerHighestPossibleWorkingValue(state.getHighestPossibleWorkingValue() * 2);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRandomChance() {
        return true;
    }

}
