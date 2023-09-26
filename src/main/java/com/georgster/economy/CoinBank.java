package com.georgster.economy;

import com.georgster.control.manager.Manageable;
import com.georgster.economy.exception.InsufficientCoinsException;

/**
 * A container for all the coins, called a balance, a {@code Member} has.
 * <p>
 * If withdrawls exceeding the balance of a {@link CoinBank} is requested,
 * an {@link InsufficientCoinsException} is thrown.
 * <p>
 * This {@link Manageable Manageable's} identifier is the member's ID.
 */
public class CoinBank implements Manageable {
    private final String memberId;
    private long balance;

    /**
     * Creates a new CoinBank for the provided member.
     * Generally used to create a bank for a new Member.
     * 
     * @param memberId The member's Snowflake ID.
     */
    public CoinBank(String memberId) {
        this.memberId = memberId;
        this.balance = 0;
    }

    /**
     * Creates a new CoinBank for the provided member
     * of the provided balance.
     * 
     * @param memberId The member's snowflake ID.
     * @param balance The member's coin balance.
     */
    public CoinBank(String memberId, long balance) {
        this.memberId = memberId;
        this.balance = balance;
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return getMemberId();
    }

    /**
     * Returns the snowflake ID for this bank's member.
     * 
     * @return The snowflake ID for this bank's member.
     */
    public String getMemberId() {
        return this.memberId;
    }

    /**
     * Returns the balance of this bank.
     * 
     * @return The balance of this bank.
     */
    public long getBalance() {
        return this.balance;
    }

    /**
     * Returns true if this bank's balance equals or
     * exceeds the provided amount.
     * 
     * @param amount The amount to check for.
     * @return True if this bank has sufficient balance, false otherwise.
     */
    public boolean hasBalance(long amount) {
        return amount <= balance;
    }

    /**
     * Transfers the provided amount from this bank to the provided bank,
     * if this bank has enough balance.
     * 
     * @param amount The amount to transfer.
     * @param transferBank The bank to transfer to.
     * @throws InsufficientCoinsException If this bank does not have sufficient balance.
     */
    public void transferTo(long amount, CoinBank transferBank) throws InsufficientCoinsException {
        this.withdrawl(amount);
        transferBank.deposit(amount);
    }

    /**
     * Transfer the provided amount from the provided bank to this bank,
     * if the provided bank has enough balance.
     * 
     * @param amount The amount to transfer.
     * @param transferBank The bank to transfer from.
     * @throws InsufficientCoinsException If the provided bank does not have sufficient balance.
     */
    public void transferFrom(long amount, CoinBank transferBank) throws InsufficientCoinsException {
        transferBank.withdrawl(amount);
        this.deposit(amount);
    }

    /**
     * Deposits the provided amount into this bank unconditionally.
     * 
     * @param amount The amount to deposit.
     */
    public void deposit(long amount) {
        this.balance += amount;
    }

    /**
     * Withdrawls the provided amount from this bank unconditionally.
     * 
     * @param amount The amount to withdrawl.
     * @throws InsufficientCoinsException If this bank does not have enough balance for the withdrawl.
     */
    public void withdrawl(long amount) throws InsufficientCoinsException {
        if (hasBalance(amount)) {
            this.balance -= amount;
        } else {
            throw new InsufficientCoinsException(this, amount);
        }
    }
}
