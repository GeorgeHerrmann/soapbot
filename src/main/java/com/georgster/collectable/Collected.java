package com.georgster.collectable;

import java.time.Instant;

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

/**
 * A {@link Collectable} that has been collected by a {@link UserProfile}.
 * <p>
 * {@link Collected Collecteds} are {@link Tradeable} and {@link Manageable},
 * as well as {@link DateTimed} at the time of creation.
 */
public final class Collected extends DateTimed implements Manageable, Tradeable {
    private final String id;
    private String memberId;
    private boolean affectsCost; // Affects the cost of the collectable
    private boolean isOnMarket; // Is on "collectable market" for a custom amount
    private long currentMarketPrice; // Custom collectable market amount
    private long recentPurchasePrice;
    private final int edition;
    private final CollectableContext collectable;

    /**
     * Constructs a {@link Collected} with the given parameters.
     * 
     * @param memberId The {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that collected the {@code Collectable}.
     * @param recentPurchasePrice The price the {@code Collectable} was purchased for.
     * @param collectable The {@link CollectableContext} of the {@code Collectable}.
     */
    public Collected(String memberId, long recentPurchasePrice, Collectable collectable) {
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = UniqueIdFactory.createId();
        this.collectable = collectable.getContext();
        this.isOnMarket = false;
        this.currentMarketPrice = collectable.getCost();
        this.edition = collectable.getNextEdition();
        this.affectsCost = !(recentPurchasePrice == 1 && collectable.getCost() == 1);
    }

    /**
     * Constructs a {@link Collected} with the given parameters.
     * 
     * @param memberId The {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that collected the {@code Collectable}.
     * @param id The {@code id} of the {@code Collected}.
     * @param recentPurchasePrice The price the {@code Collectable} was purchased for.
     * @param collectable The {@link CollectableContext} of the {@code Collectable}.
     * @param date The date the {@code Collected} was created.
     * @param time The time the {@code Collected} was created.
     * @param isOnMarket Whether or not the {@code Collected} is on the market.
     * @param currentMarketPrice The current market price of the {@code Collected}.
     * @param edition The edition of the {@code Collected}.
     * @param affectsCost Whether or not the {@code Collected} affects the cost of the {@code Collectable}.
     */
    public Collected(String memberId, String id, long recentPurchasePrice, CollectableContext collectable, String date, String time, boolean isOnMarket, long currentMarketPrice, int edition, boolean affectsCost) {
        super(date, time);
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = id;
        this.collectable = collectable;
        this.isOnMarket = isOnMarket;
        this.currentMarketPrice = currentMarketPrice;
        this.edition = edition;
        this.affectsCost = affectsCost;
    }

    /**
     * Constructs a {@link Collected} with the given parameters.
     * 
     * @param memberId The {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that collected the {@code Collectable}.
     * @param id The {@code id} of the {@code Collected}.
     * @param recentPurchasePrice The price the {@code Collectable} was purchased for.
     * @param collectable The {@link CollectableContext} of the {@code Collectable}.
     * @param date The date the {@code Collected} was created.
     * @param time The time the {@code Collected} was created.
     * @param isOnMarket Whether or not the {@code Collected} is on the market.
     * @param currentMarketPrice The current market price of the {@code Collected}.
     * @param edition The edition of the {@code Collected}.
     */
    public Collected(String memberId, String id, long recentPurchasePrice, CollectableContext collectable, String date, String time, boolean isOnMarket, long currentMarketPrice, int edition) {
        super(date, time);
        this.memberId = memberId;
        this.recentPurchasePrice = recentPurchasePrice;
        this.id = id;
        this.collectable = collectable;
        this.isOnMarket = isOnMarket;
        this.currentMarketPrice = currentMarketPrice;
        this.edition = edition;
        this.affectsCost = true;
    }

    public void fix() {
        this.affectsCost = true;
    }

    /**
     * Constructs a {@link Collected} with the given parameters.
     * 
     * @return the {@code Collected}.
     */
    public int getEdition() {
        return edition;
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return getIdentifier();
    }

    /**
     * Returns the {@code id} of the {@link com.georgster.profile.UserProfile UserProfile} that collected the {@link Collected}.
     * 
     * @return the {@code id} of the {@code UserProfile} that collected the {@code Collected}.
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * Returns the price the {@link Collectable} was purchased for.
     * 
     * @return the price the {@code Collectable} was purchased for.
     */
    public long getRecentPurchasePrice() {
        return recentPurchasePrice;
    }

    /**
     * Returns the {@link CollectableContext} of the {@link Collectable}.
     * 
     * @return the {@code CollectableContext} of the {@code Collectable}.
     */
    public CollectableContext getCollectable() {
        return collectable;
    }

    /**
     * Returns whether or not the {@link Collected} is on the market.
     * 
     * @return {@code true} if the {@code Collected} is on the market, {@code false} otherwise.
     */
    public boolean isOnMarket() {
        return isOnMarket;
    }

    /**
     * Returns whether or not the {@link Collected} affects the cost of the {@link Collectable}.
     * 
     * @return {@code true} if the {@code Collected} affects the cost of the {@code Collectable}, {@code false} otherwise.
     */
    public boolean affectsCost() {
        return affectsCost;
    }

    /**
     * Returns the current market price of the {@link Collected}.
     * 
     * @return the current market price of the {@code Collected}.
     */
    public long getCurrentMarketPrice() {
        return currentMarketPrice;
    }

    /**
     * Sets whether or not the {@link Collected} is on the market.
     * 
     * @param isOnMarket {@code true} if the {@code Collected} is on the market, {@code false} otherwise.
     */
    public void setOnMarket(boolean isOnMarket) {
        this.isOnMarket = isOnMarket;
    }

    /**
     * Sets the current market price of the {@link Collected}.
     * 
     * @param currentMarketPrice The new current market price of the {@code Collected}.
     */
    public void setCurrentMarketPrice(long currentMarketPrice) {
        this.currentMarketPrice = currentMarketPrice;
    }

    /**
     * {@inheritDoc}
     */
    public void trade(UserProfile owner, UserProfile reciever) {
        owner.removeCollected(this);
        reciever.addCollected(this);
        this.memberId = reciever.getMemberId();
    }

    /**
     * Sets the recent purchase price of the {@link Collected}.
     * 
     * @param recentPurchasePrice The new recent purchase price of the {@code Collected}.
     */
    public void setRecentPurchasePrice(long recentPurchasePrice) {
        this.recentPurchasePrice = recentPurchasePrice;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return collectable.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + getCollectable().getDescription() + "*\n\n");
        sb.append("ID: " + getIdentifier() + "\n");
        sb.append("*Purchased for **" + getRecentPurchasePrice() + "** coins*");
        return sb.toString();
    }

    /**
     * Returns a detailed {@link String} representation of the {@link Collected}.
     * 
     * @param manager The {@link UserProfileManager} to get the {@link com.georgster.profile.UserProfile UserProfile} from.
     * @param collectableManager The {@link CollectableManager} to get the {@link Collectable} from.
     * @return a detailed {@code String} representation of the {@code Collected}.
     */
    public String toDetailedString(UserProfileManager manager, CollectableManager collectableManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + getCollectable().getDescription() + "*\n\n");
        sb.append("Rarity: ***" + collectable.getRarity(manager, collectableManager).toString() + "***\n");
        sb.append("ID: " + getIdentifier() + "\n");
        sb.append("*Purchased for **" + getRecentPurchasePrice() + "** coins*\n");
        if (isOnMarket) {
            sb.append("*On market for **" + getCurrentMarketPrice() + "** coins*");
        } else {
            sb.append("*Not on market*");
        }
        return sb.toString();
    }

    /**
     * Returns an {@link EmbedCreateSpec} of the {@link Collected}.
     * 
     * @param userManager The {@link UserProfileManager} to get the {@link com.georgster.profile.UserProfile UserProfile} from.
     * @param manager The {@link CollectableManager} to get the {@link Collectable} from.
     * @return an {@code EmbedCreateSpec} of the {@code Collected}.
     */
    public EmbedCreateSpec getDetailedEmbed(UserProfileManager userManager, CollectableManager manager) {
        Collectable c = manager.get(this.getName());
        Member owner = new GuildInteractionHandler(manager.getGuild()).getMemberById(this.getMemberId());

        return EmbedCreateSpec.builder()
                //.title(owner.getDisplayName() + "'s " + collectable.getName())
                .description(this.toDetailedString(userManager, manager))
                .timestamp(Instant.parse(getRawDateTime()))
                .author(owner.getDisplayName() + "'s " + c.getName(), null, owner.getAvatarUrl())
                .footer(this.getEdition() + " of " + c.getMaxEdition(), Collectable.editionIconUrl())
                .image(collectable.getImageUrl())
                .color(Collectable.getRarityColor(collectable.getRarity(userManager, manager)))
                .build();
    }

    /**
     * Returns an {@link EmbedCreateSpec} of the {@link Collected}.
     * 
     * @param userManager The {@link UserProfileManager} to get the {@link com.georgster.profile.UserProfile UserProfile} from.
     * @param manager The {@link CollectableManager} to get the {@link Collectable} from.
     * @return an {@code EmbedCreateSpec} of the {@code Collected}.
     */
    public EmbedCreateSpec getGeneralEmbed(UserProfileManager userManager, CollectableManager manager) {
        Collectable c = manager.get(this.getName());
        Member owner = new GuildInteractionHandler(manager.getGuild()).getMemberById(this.getMemberId());

        return EmbedCreateSpec.builder()
                //.title(owner.getDisplayName() + "'s " + c.getName())
                .description(this.toString() + "\nRarity: ***" + collectable.getRarity(userManager, manager).toString() + "***")
                .timestamp(Instant.parse(getRawDateTime()))
                .author(owner.getDisplayName() + "'s " + c.getName(), null, owner.getAvatarUrl())
                .footer(this.getEdition() + " of " + c.getMaxEdition(), Collectable.editionIconUrl())
                .image(collectable.getImageUrl())
                .color(Collectable.getRarityColor(collectable.getRarity(userManager, manager)))
                .build();
    }
}
