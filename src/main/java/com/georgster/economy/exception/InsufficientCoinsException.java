package com.georgster.economy.exception;

import com.georgster.economy.CoinBank;

/**
 * A {@link RuntimeException} thrown when coins are attempted to be withdrawn from a bank with insufficient funds.
 */
public class InsufficientCoinsException extends RuntimeException {
    
    /**
     * Creates a new InsufficientCoinsException for a withdrawl of {@code desiredWithdrawlAmount} from {@link CoinBank} {@code bank}.
     * 
     * @param bank The {@link CoinBank} attempting to withdrawl from.
     * @param desiredWithdrawlAmount The amount attempted to withdrawl from the bank
     */
    public InsufficientCoinsException(CoinBank bank, long desiredWithdrawlAmount) {
        super("Bank for user " + bank.getIdentifier() + " has insufficient balance of " + bank.getBalance() + " for withdrawl of " + desiredWithdrawlAmount);
    }

}
