package com.georgster.collectable.trade;

import java.util.ArrayList;
import java.util.List;

import com.georgster.profile.UserProfile;
import com.georgster.util.DateTimed;

/**
 * A {@link TradeOffer} that can be sent between two {@link UserProfile UserProfiles}.
 * <p>
 * A {@link TradeOffer} is {@link DateTimed} at the time of creation.
 */
public final class TradeOffer extends DateTimed {
    private UserProfile offerer;
    private UserProfile reciever;

    private final List<Tradeable> offeredItems;
    private long offeredCoins;
    private final List<Tradeable> requestedItems;
    private long requestedCoins;

    /**
     * Constructs a {@link TradeOffer} with the given parameters.
     * 
     * @param offeredItems The {@link Tradeable Tradeables} offered in the trade.
     * @param offeredCoins The coins offered in the trade.
     * @param requestedItems The {@code Tradeables} requested in the trade.
     * @param requestedCoins The coins requested in the trade.
     * @param offerer The {@link UserProfile} that sent the trade.
     * @param reciever The {@code UserProfile} that recieved the trade.
     */
    public TradeOffer(List<Tradeable> offeredItems, long offeredCoins, List<Tradeable> requestedItems, long requestedCoins, UserProfile offerer, UserProfile reciever) {
        this.offeredItems = offeredItems;
        this.offeredCoins = offeredCoins;
        this.requestedItems = requestedItems;
        this.requestedCoins = requestedCoins;
        this.offerer = offerer;
        this.reciever = reciever;
    }

    /**
     * Constructs a {@link TradeOffer} with the given parameters.
     * 
     * @param offeredItems The {@link Tradeable Tradeables} offered in the trade.
     * @param requestedItems The {@code Tradeables} requested in the trade.
     * @param offerer The {@link UserProfile} that sent the trade.
     * @param reciever The {@code UserProfile} that recieved the trade.
     */
    public TradeOffer(List<Tradeable> offeredItems, UserProfile offerer, UserProfile reciever) {
        this.offeredItems = offeredItems;
        this.offeredCoins = 0;
        this.requestedItems = new ArrayList<>();
        this.requestedCoins = 0;
        this.offerer = offerer;
        this.reciever = reciever;
    }

    /**
     * Constructs a {@link TradeOffer} with the given parameters.
     * 
     * @param offerer The {@link UserProfile} that sent the trade.
     * @param reciever The {@code UserProfile} that recieved the trade.
     */
    public TradeOffer(UserProfile offerer, UserProfile reciever) {
        this.offeredItems = new ArrayList<>();
        this.offeredCoins = 0;
        this.requestedItems = new ArrayList<>();
        this.requestedCoins = 0;
        this.offerer = offerer;
        this.reciever = reciever;
    }

    /**
     * Returns the {@link Tradeable Tradeables} offered in the trade.
     * 
     * @return the {@code Tradeables} offered in the trade.
     */
    public List<Tradeable> getOfferedItems() {
        return offeredItems;
    }

    /**
     * Returns the coins offered in the trade.
     * 
     * @return the coins offered in the trade.
     */
    public long getOfferedCoins() {
        return offeredCoins;
    }

    /**
     * Returns the {@link Tradeable Tradeables} requested in the trade.
     * 
     * @return the {@code Tradeables} requested in the trade.
     */
    public List<Tradeable> getRequestedItems() {
        return requestedItems;
    }

    /**
     * Returns the coins requested in the trade.
     * 
     * @return the coins requested in the trade.
     */
    public long getRequestedCoins() {
        return requestedCoins;
    }

    /**
     * Sets the coins offered in the trade.
     * 
     * @param offeredCoins the coins offered in the trade.
     */
    public void setOfferedCoins(long offeredCoins) {
        this.offeredCoins = offeredCoins;
    }

    /**
     * Sets the coins requested in the trade.
     * 
     * @param requestedCoins the coins requested in the trade.
     */
    public void setRequestedCoins(long requestedCoins) {
        this.requestedCoins = requestedCoins;
    }

    /**
     * Sets the {@link Tradeable Tradeables} offered in the trade.
     * 
     * @param requestedItems the {@code Tradeables} offered in the trade.
     */
    public void setRequestedItems(List<Tradeable> requestedItems) {
        this.requestedItems.clear();
        this.requestedItems.addAll(requestedItems);
    }

    /**
     * Adds a {@link Tradeable} to the {@link Tradeable Tradeables} offered in the trade.
     * 
     * @param requestedItem the {@code Tradeable} to add.
     */
    public void addRequestedItem(Tradeable requestedItem) {
        this.requestedItems.add(requestedItem);
    }

    /**
     * Removes a {@link Tradeable} from the {@link Tradeable Tradeables} offered in the trade.
     * 
     * @param requestedItem the {@code Tradeable} to remove.
     */
    public void removeRequestedItem(Tradeable requestedItem) {
        this.requestedItems.remove(requestedItem);
    }

    /**
     * Sets the {@link Tradeable Tradeables} requested in the trade.
     * 
     * @param offeredItems the {@code Tradeables} requested in the trade.
     */
    public void setOfferedItems(List<Tradeable> offeredItems) {
        this.offeredItems.clear();
        this.offeredItems.addAll(offeredItems);
    }

    /**
     * Adds a {@link Tradeable} to the {@link Tradeable Tradeables} requested in the trade.
     * 
     * @param offeredItem the {@code Tradeable} to add.
     */
    public void addOfferedItem(Tradeable offeredItem) {
        this.offeredItems.add(offeredItem);
    }

    /**
     * Removes a {@link Tradeable} from the {@link Tradeable Tradeables} requested in the trade.
     * 
     * @param offeredItem the {@code Tradeable} to remove.
     */
    public void removeOfferedItem(Tradeable offeredItem) {
        this.offeredItems.remove(offeredItem);
    }

    /**
     * Returns the {@link UserProfile} that sent the trade.
     * 
     * @return the {@code UserProfile} that sent the trade.
     */
    public UserProfile getOfferer() {
        return offerer;
    }

    /**
     * Returns the {@link UserProfile} that recieved the trade.
     * 
     * @return the {@code UserProfile} that recieved the trade.
     */
    public UserProfile getReciever() {
        return reciever;
    }

    /**
     * Sets the {@link UserProfile} that sent the trade.
     * 
     * @param offerer the {@code UserProfile} that sent the trade.
     */
    public void executeTrade() {
        offeredItems.forEach(item -> item.trade(offerer, reciever));
        requestedItems.forEach(item -> item.trade(reciever, offerer));
        offerer.getBank().withdrawl(offeredCoins);
        reciever.getBank().withdrawl(requestedCoins);
        offerer.getBank().deposit(requestedCoins);
        reciever.getBank().deposit(offeredCoins);
    }
}
