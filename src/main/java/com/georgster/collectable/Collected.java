package com.georgster.collectable;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.UniqueIdFactory;
import com.georgster.profile.UserProfile;
import com.georgster.util.DateTimed;

public final class Collected extends DateTimed implements Manageable, Tradeable {
    private final String id;
    private String memberId;
    private long recentPurchasePrice;
    private final CollectableContext collectable;

    // new
    public Collected(String memberId, long recentPurchasePrice, CollectableContext collectable) {
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = UniqueIdFactory.createId();
        this.collectable = collectable;
    }

    // from database
    public Collected(String memberId, String id, long recentPurchasePrice, CollectableContext collectable, String date, String time) {
        super(date, time);
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = id;
        this.collectable = collectable;
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
