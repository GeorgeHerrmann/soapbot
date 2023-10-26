package com.georgster.collectable;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.UniqueIdFactory;
import com.georgster.profile.UserProfile;
import com.georgster.util.DateTimed;

public final class Collected extends DateTimed implements Manageable, Tradeable {
    private final String id;
    private String memberId;
    private boolean isOnMarket; // Is on "collectable market" for a custom amount
    private long currentMarketPrice; // Custom collectable market amount
    private long recentPurchasePrice;
    private final CollectableContext collectable;

    // new
    public Collected(String memberId, long recentPurchasePrice, CollectableContext collectable) {
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = UniqueIdFactory.createId();
        this.collectable = collectable;
        this.isOnMarket = false;
        this.currentMarketPrice = collectable.getCost();
    }

    // from database
    public Collected(String memberId, String id, long recentPurchasePrice, CollectableContext collectable, String date, String time, boolean isOnMarket, long currentMarketPrice) {
        super(date, time);
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = id;
        this.collectable = collectable;
        this.isOnMarket = isOnMarket;
        this.currentMarketPrice = currentMarketPrice;
    }

    public String getIdentifier() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public long getRecentPurchasePrice() {
        return recentPurchasePrice;
    }

    public CollectableContext getCollectable() {
        return collectable;
    }

    public boolean isOnMarket() {
        return isOnMarket;
    }

    public long getCurrentMarketPrice() {
        return currentMarketPrice;
    }

    public void setOnMarket(boolean isOnMarket) {
        this.isOnMarket = isOnMarket;
    }

    public void setCurrentMarketPrice(long currentMarketPrice) {
        this.currentMarketPrice = currentMarketPrice;
    }

    public void trade(UserProfile owner, UserProfile reciever) {
        owner.removeCollected(this);
        reciever.addCollected(this);
        this.memberId = reciever.getMemberId();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCollectable().getName() + "\n");
        sb.append("ID: " + getIdentifier() + "\n");
        sb.append("Bought at " + getFormattedTime() + " on " + getFormattedDate() + "\n");
        sb.append("Purchased for " + getRecentPurchasePrice() + " coins");
        return sb.toString();
    }
}
