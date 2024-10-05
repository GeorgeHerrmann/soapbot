package com.georgster.coinfarm.model.upgrades;

/**
 * A {@link FactoryUpgrade} that represents the absence of a {@link FactoryUpgrade}.
 * <p>
 * An {@link AbsentFactoryUpgrade} should never be part of a Member's {@link com.georgster.coinfarm.model.CoinFactory CoinFactory}
 * and cannot be applied to a {@link com.georgster.coinfarm.model.upgrades.CoinProductionState CoinProductionState}.
 */
public final class AbsentFactoryUpgrade extends FactoryUpgrade {
    
    /**
     * Creates a new {@link AbsentFactoryUpgrade} representing the absence of a {@link FactoryUpgrade}.
     */
    public AbsentFactoryUpgrade() {
        super("No Upgrade", "None", "No Upgrade has been purchased for this upgrade track", 0, 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * An {@link AbsentFactoryUpgrade} cannot be applied to a {@link CoinProductionState} and will throw an {@link UnsupportedOperationException}.
     * 
     * @throws UnsupportedOperationException always.
     */
    public void applyUpgrade(CoinProductionState state) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("An absent upgrade cannot be applied to the CoinProductionState");
    }

    /**
     * {@inheritDoc}
     * <p>
     * An {@link AbsentFactoryUpgrade} is never owned and will always return {@code false}.
     */
    @Override
    public boolean isOwned() {
        return false;
    }
    
}
