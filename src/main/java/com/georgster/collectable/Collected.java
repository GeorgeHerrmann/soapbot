package com.georgster.collectable;

import com.georgster.collectable.trade.Tradeable;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.Manageable;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.identify.UniqueIdFactory;
import com.georgster.profile.UserProfile;
import com.georgster.util.DateTimed;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;

public final class Collected extends DateTimed implements Manageable, Tradeable {
    private final String id;
    private String memberId;
    private boolean isOnMarket; // Is on "collectable market" for a custom amount
    private long currentMarketPrice; // Custom collectable market amount
    private long recentPurchasePrice;
    private final int edition;
    private final CollectableContext collectable;

    // new
    public Collected(String memberId, long recentPurchasePrice, Collectable collectable) {
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = UniqueIdFactory.createId();
        this.collectable = collectable.getContext();
        this.isOnMarket = false;
        this.currentMarketPrice = collectable.getCost();
        this.edition = collectable.getNextEdition();
    }

    // from database
    public Collected(String memberId, String id, long recentPurchasePrice, CollectableContext collectable, String date, String time, boolean isOnMarket, long currentMarketPrice, int edition) {
        super(date, time);
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = id;
        this.collectable = collectable;
        this.isOnMarket = isOnMarket;
        this.currentMarketPrice = currentMarketPrice;
        this.edition = edition;
    }

    public Collected(String memberId, String id, long recentPurchasePrice, CollectableContext collectable, String date, String time, boolean isOnMarket, long currentMarketPrice) {
        super(date, time);
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = id;
        this.collectable = collectable;
        this.isOnMarket = isOnMarket;
        this.currentMarketPrice = currentMarketPrice;
        this.edition = 1;
    }

    public int getEdition() {
        return edition;
    }

    public String getIdentifier() {
        return id;
    }

    public String getId() {
        return getIdentifier();
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

    public void setRecentPurchasePrice(long recentPurchasePrice) {
        this.recentPurchasePrice = recentPurchasePrice;
    }

    public String getName() {
        return collectable.getName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCollectable().getName() + "\n");
        sb.append("ID: " + getIdentifier() + "\n");
        sb.append("Bought at " + getFormattedTime() + " on " + getFormattedDate() + "\n");
        sb.append("Purchased for " + getRecentPurchasePrice() + " coins");
        return sb.toString();
    }

    public String toDetailedString(UserProfileManager manager) {
        StringBuilder sb = new StringBuilder();
        sb.append(getCollectable().getName() + "\n");
        sb.append("*" + getCollectable().getDescription() + "*\n");
        sb.append("Rarity: " + collectable.getRarity(manager).toString() + "\n");
        sb.append("ID: " + getIdentifier() + "\n");
        sb.append("Bought at " + getFormattedTime() + " on " + getFormattedDate() + "\n");
        sb.append("Purchased for " + getRecentPurchasePrice() + " coins\n");
        if (isOnMarket) {
            sb.append("On market for **" + getCurrentMarketPrice() + "** coins");
        } else {
            sb.append("Not on market");
        }
        return sb.toString();
    }

    public EmbedCreateSpec getDetailedEmbed(UserProfileManager userManager, CollectableManager manager) {
        Collectable c = manager.get(this.getName());
        Member owner = new GuildInteractionHandler(manager.getGuild()).getMemberById(this.getMemberId());

        return EmbedCreateSpec.builder()
                .title(owner.getDisplayName() + "'s " + collectable.getName())
                .description(this.toDetailedString(userManager))
                .footer(this.getEdition() + " of " + c.getCollecteds().size(), Collectable.editionIconUrl())
                .image(collectable.getImageUrl())
                .color(Collectable.getRarityColor(collectable.getRarity(userManager)))
                .build();
    }

    public EmbedCreateSpec getGeneralEmbed(UserProfileManager userManager, CollectableManager manager) {
        Collectable c = manager.get(this.getName());
        Member owner = new GuildInteractionHandler(manager.getGuild()).getMemberById(this.getMemberId());

        return EmbedCreateSpec.builder()
                .title(owner.getDisplayName() + "'s " + c.getName())
                .description(this.toString() + "\nOwned by: " + owner.getMention() + "\nRarity: ***" + collectable.getRarity(userManager).toString() + "***")
                .footer(this.getEdition() + " of " + c.getCollecteds().size(), Collectable.editionIconUrl())
                .image(collectable.getImageUrl())
                .color(Collectable.getRarityColor(collectable.getRarity(userManager)))
                .build();
    }
}
