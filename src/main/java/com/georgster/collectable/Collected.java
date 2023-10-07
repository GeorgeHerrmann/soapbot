package com.georgster.collectable;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.UniqueIdFactory;
import com.georgster.util.DateTimed;

public final class Collected extends DateTimed implements Manageable {
    private final String id;
    private String memberId;
    private long recentPurchasePrice;
    private final Collectable collectable;

    public Collected(String memberId, long recentPurchasePrice, Collectable collectable) {
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = UniqueIdFactory.createId();
        this.collectable = collectable;
    }

    public Collected(String memberId, String id, long recentPurchasePrice, Collectable collectable) {
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

    public Collectable getCollectable() {
        return collectable;
    }
}
