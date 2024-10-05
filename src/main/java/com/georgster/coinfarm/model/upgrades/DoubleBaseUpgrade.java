package com.georgster.coinfarm.model.upgrades;

public class DoubleBaseUpgrade extends FactoryUpgrade {

    public DoubleBaseUpgrade() {
        super("double", "Working", "Doubles the **base** production value of the factory when processed", 2, 10);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue(state.getBaseProductionValue());
    }
    
}