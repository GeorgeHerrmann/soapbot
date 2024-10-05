package com.georgster.coinfarm.model.upgrades;

public class AddFiftyWorkingUpgrade extends FactoryUpgrade {

    public AddFiftyWorkingUpgrade() {
        super("fifty", "Working", "Adds 50 coins to the **working** production value of the factory when processed", 1, 1);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeWorkingProductionValue(50);
    }
    
}
