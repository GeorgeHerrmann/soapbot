package com.georgster.collectable.trade;

import java.util.ArrayList;
import java.util.List;

import com.georgster.util.DateTimed;

public final class TradeOffer extends DateTimed {
    private final List<Tradeable> offeredItems;
    private long offeredCoins;
    private final List<Tradeable> requestedItems;
    private long requestedCoins;

    public TradeOffer(List<Tradeable> offeredItems, long offeredCoins, List<Tradeable> requestedItems, long requestedCoins) {
        this.offeredItems = offeredItems;
        this.offeredCoins = offeredCoins;
        this.requestedItems = requestedItems;
        this.requestedCoins = requestedCoins;
    }

    public TradeOffer(List<Tradeable> offeredItems) {
        this.offeredItems = offeredItems;
        this.offeredCoins = 0;
        this.requestedItems = new ArrayList<>();
        this.requestedCoins = 0;
    }

    public List<Tradeable> getOfferedItems() {
        return offeredItems;
    }

    public long getOfferedCoins() {
        return offeredCoins;
    }

    public List<Tradeable> getRequestedItems() {
        return requestedItems;
    }

    public long getRequestedCoins() {
        return requestedCoins;
    }

    public void setOfferedCoins(long offeredCoins) {
        this.offeredCoins = offeredCoins;
    }

    public void setRequestedCoins(long requestedCoins) {
        this.requestedCoins = requestedCoins;
    }

    public void setRequestedItems(List<Tradeable> requestedItems) {
        this.requestedItems.clear();
        this.requestedItems.addAll(requestedItems);
    }

    public void addRequestedItem(Tradeable requestedItem) {
        this.requestedItems.add(requestedItem);
    }

    public void removeRequestedItem(Tradeable requestedItem) {
        this.requestedItems.remove(requestedItem);
    }

    public void setOfferedItems(List<Tradeable> offeredItems) {
        this.offeredItems.clear();
        this.offeredItems.addAll(offeredItems);
    }

    public void addOfferedItem(Tradeable offeredItem) {
        this.offeredItems.add(offeredItem);
    }

    public void removeOfferedItem(Tradeable offeredItem) {
        this.offeredItems.remove(offeredItem);
    }
}
