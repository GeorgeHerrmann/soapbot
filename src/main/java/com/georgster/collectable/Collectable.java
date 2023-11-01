package com.georgster.collectable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.identify.util.UniqueIdentified;
import com.georgster.economy.exception.InsufficientCoinsException;
import com.georgster.profile.UserProfile;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

public final class Collectable extends UniqueIdentified {
    private static final String EDITION_ICON_URL = "https://static.thenounproject.com/png/5481694-200.png";

    private CollectableContext context;
    private final List<Collected> collecteds;

    /**
     * A Rarity for a {@link Collectable}.
     * <p>
     * A {@link Collectable Collectable's} rarity is determined as a proportion of the current cost
     * to the total amount of coins in the economy for a specific Guild (controlled by the UserProfileManager).
     */
    public enum Rarity {
        /**
         * A {@link Collectable} which has a coin cost less than 1% of the total economy.
         */
        COMMON,
        /**
         * A {@link Collectable} which has a coin cost of at least 1% of the total economy.
         */
        UNCOMMON,
        /**
         * A {@link Collectable} which has a coin cost of at least 5% of the total economy.
         */
        RARE,
        /**
         * A {@link Collectable} which has a coin cost of at least 10% of the total economy.
         */
        LEGENDARY,
        /**
         * A {@link Collectable} which has a coin cost of at least 25% of the total economy.
         * Unsuprisingly, there can be a maximum of four of these per Guild.
         */
        UNIQUE
    }

    // new
    public Collectable(String name, String ownerId, String description, String imageUrl, long initialCost) {
        super(name);
        this.context = new CollectableContext(name, ownerId, description, imageUrl, initialCost);
        this.collecteds = new ArrayList<>();
    }

    // from database
    public Collectable(CollectableContext context, List<Collected> collecteds) {
        super(context.getName());
        this.context = context;
        this.collecteds = collecteds;
    }

    public String getName() {
        return context.getName();
    }

    public String getDescription() {
        return context.getDescription();
    }

    public String getImageUrl() {
        return context.getImageUrl();
    }

    public long getCost() {
        return context.getCost();
    }

    public long getInitialCost() {
        return context.getInitialCost();
    }

    public List<Collected> getCollecteds() {
        return collecteds;
    }

    public Collected getCollected(String id) {
        return collecteds.stream().filter(collected -> collected.getIdentifier().equals(id)).findFirst().orElse(null);
    }

    public static Collectable initialize(String name, String ownerId) {
        return new Collectable(name, ownerId, "", "", 0);
    }

    public void setName(String name) {
        this.context.setName(name);
    }

    public void setDescription(String description) {
        if (description == null) return;
        this.context.setDescription(description);
    }

    public void setImageUrl(String imageUrl) {
        if (imageUrl == null) return;
        this.context.setImageUrl(imageUrl);
    }

    public void setInitialCost(long cost) {
        this.context.setInitialCost(cost);
        this.context.setCost(cost);
    }

    public CollectableContext getContext() {
        return context;
    }

    public void purchaseCollected(UserProfile profile) throws InsufficientCoinsException {
        profile.getBank().withdrawl(getCost()); // Throws InsufficientCoinsException if not enough coins
        Collected collected = new Collected(profile.getMemberId(), getCost(), this);
        profile.addCollected(collected);
        this.context.setCost(getCost() / 2);
        collecteds.add(collected);
    }

    public void sellCollected(UserProfile profile, Collected collected) {
        profile.getBank().deposit(collected.getRecentPurchasePrice());
        profile.removeCollected(collected);
        collecteds.removeIf(c -> c.getIdentifier().equals(collected.getIdentifier()));
        context.setCost(getCost() * 2);
    }

    public void sellCollected(UserProfile profile, String id) {
        Collected collected = profile.getCollecteds().stream().filter(collected1 -> collected1.getIdentifier().equals(id)).findFirst().orElse(null);
        if (collected == null) return;
        profile.getBank().deposit(collected.getRecentPurchasePrice());
        profile.removeCollected(collected);
        collecteds.removeIf(c -> c.getIdentifier().equals(collected.getIdentifier()));
        context.setCost(getCost() * 2);
    }

    public int numCards() {
        return collecteds.size();
    }

    public void setContext(CollectableContext context) {
        this.context = context;
    }

    public Rarity getRarity(UserProfileManager manager) {
        long totalCoins = manager.getTotalCoins();
        if (getCost() >= totalCoins * .25) {
            return Rarity.UNIQUE;
        } else if (getCost() >= totalCoins * .1) {
            return Rarity.LEGENDARY;
        } else if (getCost() >= totalCoins * .05) {
            return Rarity.RARE;
        } else if (getCost() >= totalCoins * .01) {
            return Rarity.UNCOMMON;
        } else {
            return Rarity.COMMON;
        }
    }

    public static Color getRarityColor(Rarity rarity) {
        if (rarity == Rarity.COMMON) {
            return Color.LIGHT_GRAY;
        } else if (rarity == Rarity.UNCOMMON) {
            return Color.GREEN;
        } else if (rarity == Rarity.RARE) {
            return Color.DEEP_SEA;
        } else if (rarity == Rarity.LEGENDARY) {
            return Color.VIVID_VIOLET;
        } else if (rarity == Rarity.UNIQUE) {
            return Color.ORANGE;
        }
        throw new IllegalArgumentException("Invalid rarity");
    }

    public String getCreatorId() {
        return context.getOwnerId();
    }

    public boolean owns(UserProfile profile) {
        return profile.getCollecteds().stream().anyMatch(collected -> collected.getCollectable().getName().equals(context.getName()));
    }

    public List<Collected> getUserCollecteds(UserProfile profile) {
        return collecteds.stream().filter(c -> c.getMemberId().equals(profile.getMemberId())).toList();
    }

    public void updateCollected(Collected collected) {
        collecteds.removeIf(c -> c.getId().equals(collected.getId()));
        collecteds.add(collected);
    }

    public static String editionIconUrl() {
        return EDITION_ICON_URL;
    }

    public int getNextEdition() {
        Set<Integer> occupiedEditions = new HashSet<>();
        
        for (Collected c : collecteds) {
            occupiedEditions.add(c.getEdition());
        }
        
        int edition = 1;
        while (occupiedEditions.contains(edition)) {
            edition++;
        }
        
        return edition;
    }

    public int getHighestEdition() {
        int highestEdition = 0;
    
        for (Collected c : collecteds) {
            if (c.getEdition() > highestEdition) {
                highestEdition = c.getEdition();
            }
        }
    
        return highestEdition;
    }

    public EmbedCreateSpec getGeneralEmbed(UserProfileManager userManager) {
        GuildInteractionHandler guildHandler = new GuildInteractionHandler(userManager.getGuild());
        return EmbedCreateSpec.builder()
            .title(this.getName())
            .description(this.toString() + "\nRarity: ***" + this.getRarity(userManager).toString() + "***\nCreated by: " + guildHandler.getMemberById(this.getCreatorId()).getMention())
            .image(this.getImageUrl())
            .color(Collectable.getRarityColor(this.getRarity(userManager)))
            .build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("*" + getDescription() + "*\n");
        sb.append("Cost: " + "*" + getCost() + "*\n");
        sb.append("Initial Cost: " + "*" + getInitialCost() + "*");
        
        return sb.toString();
    }

}
