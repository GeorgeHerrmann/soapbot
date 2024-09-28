package com.georgster.coinfarm.model.upgrades;

public class AddFiftyWorkingUpgrade extends FactoryUpgrade {

    public AddFiftyWorkingUpgrade() {
        super("fifty", 1, 10);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProductionValue(50);
    }
    
}
