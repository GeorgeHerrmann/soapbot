package com.georgster.economy;

import com.georgster.control.manager.Manageable;

public class CoinBank implements Manageable {
    private final String memberId;
    private long balance;

    public CoinBank(String memberId) {
        this.memberId = memberId;
        this.balance = 0;
    }

    public CoinBank(String memberId, long balance) {
        this.memberId = memberId;
        this.balance = balance;
    }

    public String getIdentifier() {
        return getMemberId();
    }

    public String getMemberId() {
        return this.memberId;
    }

    public long getBalance() {
        return this.balance;
    }

    public boolean hasBalance(long amount) {
        return amount <= balance;
    }

    public void transferTo(long amount, CoinBank transferBank) throws IllegalArgumentException {
        this.withdrawl(amount);
        transferBank.deposit(amount);
    }

    public void transferFrom(long amount, CoinBank transferBank) throws IllegalArgumentException {
        transferBank.withdrawl(amount);
        this.deposit(amount);
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public void withdrawl(long amount) throws IllegalArgumentException {
        if (hasBalance(amount)) {
            this.balance -= amount;
        } else {
            throw new IllegalArgumentException("Balance of: " + balance + " insufficient for a withdrawl of: " + amount + ".");
        }
    }
}
