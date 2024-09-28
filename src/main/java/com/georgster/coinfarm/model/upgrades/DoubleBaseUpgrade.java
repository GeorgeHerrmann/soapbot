package com.georgster.coinfarm.model.upgrades;

public class DoubleBaseUpgrade extends FactoryUpgrade {

    public DoubleBaseUpgrade() {
        super("double", 2, 10);
    }

    public void applyUpgrade(CoinProductionState state) {
        state.upgradeBaseProductionValue(state.getBaseProductionValue());
    }
    
}