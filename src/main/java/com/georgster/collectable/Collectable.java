package com.georgster.collectable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.identify.util.UniqueIdentified;
import com.georgster.economy.exception.InsufficientCoinsException;
import com.georgster.profile.UserProfile;
import com.georgster.util.handler.GuildInteractionHandler;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

/**
 * A {@link UniqueIdentified} object that represents a unique item that can be purchased by a {@link UserProfile} and added to their collection.
 * <p>
 * A {@link Collectable} provides a definition for the general properties of a trading card, such as its name, description, and image,
 * whereas {@link Collected Collecteds} are the actual cards that are purchased by a {@link UserProfile} and added to their collection.
 * <p>
 * A {@link Collectable} keeps track of all its {@link Collected Collecteds} and is defined by a {@link CollectableContext}.
 */
public final class Collectable extends UniqueIdentified {
    private static final String EDITION_ICON_URL = "https://static.thenounproject.com/png/5481694-200.png";

    private CollectableContext context;
    private boolean adjustOnBuy; // Adjust cost on buy if selling caused decimal truncation
    private boolean isLocked;
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

    /**
     * Creates a new {@link Collectable} with the specified name, owner, description, image, and initial cost.
     * 
     * @param name The name of the {@link Collectable}.
     * @param ownerId The id of the {@link UserProfile} that created the {@link Collectable}.
     * @param description The description of the {@link Collectable}.
     * @param imageUrl The image url of the {@link Collectable}.
     * @param initialCost The initial cost of the {@link Collectable}.
     */
    public Collectable(String name, String ownerId, String description, String imageUrl, long initialCost) {
        super(name);
        this.context = new CollectableContext(name, ownerId, description, imageUrl, initialCost);
        this.adjustOnBuy = false;
        this.collecteds = new ArrayList<>();
        this.isLocked = false;
    }

    /**
     * Creates a new {@link Collectable} with the specified {@link CollectableContext} and {@link Collected Collecteds}.
     * 
     * @param context The {@link CollectableContext} of the {@link Collectable}.
     * @param collecteds The {@link Collected Collecteds} of the {@link Collectable}.
     * @param adjustOnBuy Whether or not the {@link Collectable} should adjust its cost on purchase.
     */
    public Collectable(CollectableContext context, List<Collected> collecteds, boolean adjustOnBuy, boolean isLocked) {
        super(context.getName());
        this.context = context;
        this.adjustOnBuy = adjustOnBuy;
        this.collecteds = collecteds;
        this.isLocked = isLocked;
    }

    /**
     * Creates a new {@link Collectable} with the specified {@link CollectableContext} and {@link Collected Collecteds}.
     * 
     * @param context The {@link CollectableContext} of the {@link Collectable}.
     * @param collecteds The {@link Collected Collecteds} of the {@link Collectable}.
     * @param adjustOnBuy Whether or not the {@link Collectable} should adjust its cost on purchase.
     */
    public Collectable(CollectableContext context, List<Collected> collecteds, boolean adjustOnBuy) {
        super(context.getName());
        this.context = context;
        this.adjustOnBuy = adjustOnBuy;
        this.collecteds = collecteds;
        this.isLocked = false;
    }

    /**
     * Returns the name of the {@link Collectable}.
     * 
     * @return the name of the {@code Collectable}.
     */
    public String getName() {
        return context.getName();
    }

    /**
     * Returns the description of the {@link Collectable}.
     * 
     * @return the description of the {@code Collectable}.
     */
    public String getDescription() {
        return context.getDescription();
    }

    /**
     * Returns the image url of the {@link Collectable}.
     * 
     * @return the image url of the {@code Collectable}.
     */
    public String getImageUrl() {
        return context.getImageUrl();
    }

    /**
     * Returns the cost of the {@link Collectable}.
     * 
     * @return the cost of the {@code Collectable}.
     */
    public long getCost() {
        return context.getCost();
    }

    /**
     * Returns the initial cost of the {@link Collectable}.
     * 
     * @return the initial cost of the {@code Collectable}.
     */
    public long getInitialCost() {
        return context.getInitialCost();
    }

    /**
     * Returns the {@link Collected Collecteds} of the {@link Collectable}.
     * 
     * @return the {@code Collecteds} of the {@code Collectable}.
     */
    public List<Collected> getCollecteds() {
        return collecteds;
    }

    /**
     * Returns whether or not the {@link Collectable} is locked, meaning it cannot be purchased.
     * 
     * @return {@code true} if the {@code Collectable} is locked, {@code false} otherwise.
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Locks or unlocks the {@link Collectable}.
     * 
     * @param setting {@code true} to lock the {@code Collectable}, {@code false} to unlock it.
     * @throws IllegalStateException if the {@code Collectable} has no {@link Collected Collecteds} or more than one {@code Collected}.
     */
    public void lock(boolean setting) throws IllegalStateException {
        if (setting) {
            if (numCards() > 1) {
                throw new IllegalStateException("Cannot lock a collectable with more than one card");
            } else if (numCards() < 1) {
                throw new IllegalStateException("Cannot lock a collectable with no cards");
            } else {
                this.isLocked = true;
            }
        } else {
            this.isLocked = false;
        }
    }

    /**
     * Returns the {@link Collected} with the given id, or {@code null} if none exists.
     * 
     * @param id the id of the {@code Collected} to get.
     * @return the {@code Collected} with the given id, or {@code null} if none exists.
     */
    public Collected getCollected(String id) {
        return collecteds.stream().filter(collected -> collected.getIdentifier().equals(id)).findFirst().orElse(null);
    }

    /**
     * Factory method for creating a new {@link Collectable} with the specified name and owner.
     * 
     * @param name The name of the {@link Collectable}.
     * @param ownerId The id of the {@link UserProfile} that created the {@link Collectable}.
     * @return a new {@code Collectable} with the specified name and owner.
     */
    public static Collectable initialize(String name, String ownerId) {
        return new Collectable(name, ownerId, "", "", 0);
    }

    /**
     * Sets the name of the {@link Collectable}.
     * 
     * @param name the name of the {@code Collectable}.
     */
    public void setName(String name) {
        this.context.setName(name);
    }

    /**
     * Sets the description of the {@link Collectable}.
     * 
     * @param description the description of the {@code Collectable}.
     */
    public void setDescription(String description) {
        if (description == null) return;
        this.context.setDescription(description);
    }

    /**
     * Sets the image url of the {@link Collectable}.
     * 
     * @param imageUrl the image url of the {@code Collectable}.
     */
    public void setImageUrl(String imageUrl) {
        if (imageUrl == null) return;
        this.context.setImageUrl(imageUrl);
    }

    /**
     * Sets the cost of the {@link Collectable}.
     * 
     * @param cost the cost of the {@code Collectable}.
     */
    public void setInitialCost(long cost) {
        this.context.setInitialCost(cost);
        this.context.setCost(cost);
    }

    /**
     * Returns the {@link CollectableContext} of the {@link Collectable}.
     * 
     * @return the {@code CollectableContext} of the {@code Collectable}.
     */
    public CollectableContext getContext() {
        return context;
    }

    /**
     * Purchases a {@link Collectable} for the specified {@link UserProfile}.
     * 
     * @param profile the {@code UserProfile} to purchase the {@code Collectable} for.
     * @throws InsufficientCoinsException if the {@code UserProfile} does not have enough coins.
     * @throws IllegalStateException If this {@link Collectable} is locked.
     */
    public void purchaseCollected(UserProfile profile) throws InsufficientCoinsException, IllegalStateException {
        if (isLocked()) {
            throw new IllegalStateException("Cannot purchase a locked card");
        }
        profile.getBank().withdrawl(getCost()); // Throws InsufficientCoinsException if not enough coins
        Collected collected = new Collected(profile.getMemberId(), getCost(), this);
        profile.addCollected(collected);

        if (getCost() != 1 && getCost() % 2 == 1) {
            System.out.println("adjust with cost " + getCost());
            this.adjustOnBuy = true;
        }

        long newCost = getCost() / 2;
        if (newCost <= 1) {
            this.context.setCost(1);
        } else {
            this.context.setCost(newCost);
        }
        collecteds.add(collected);
        collecteds.forEach(c -> c.getCollectable().setCost(getCost()));
    }

    /**
     * Sells a {@link Collected} for the specified {@link UserProfile}.
     * 
     * @param profile the {@code UserProfile} to sell the {@code Collected} for.
     * @param collected the {@code Collected} to sell.
     */
    public void sellCollected(UserProfile profile, Collected collected) {
        profile.removeCollected(collected);
        collecteds.removeIf(c -> c.getIdentifier().equals(collected.getIdentifier()));

        if (collecteds.isEmpty()) {
            lock(false);
        }

        if (collected.affectsCost()) {
            profile.getBank().deposit(adjustOnBuy ? getCost() * 2 + 1 : getCost() * 2);
            if (adjustOnBuy && getCost() != 1) {
                this.context.setCost(getCost() * 2 + 1);
                adjustOnBuy = false;
            } else {
                this.context.setCost(getCost() * 2);
            }
        } else {
            profile.getBank().deposit(1);
        }
        collecteds.forEach(c -> c.getCollectable().setCost(getCost()));
    }

    /**
     * Sells a {@link Collected} for the specified {@link UserProfile}.
     * 
     * @param profile the {@code UserProfile} to sell the {@code Collected} for.
     * @param id the id of the {@code Collected} to sell.
     */
    public void sellCollected(UserProfile profile, String id) {
        Collected collected = profile.getCollecteds().stream().filter(collected1 -> collected1.getIdentifier().equals(id)).findFirst().orElse(null);
        if (collected == null) return;
        profile.removeCollected(collected);
        collecteds.removeIf(c -> c.getIdentifier().equals(collected.getIdentifier()));

        if (collecteds.isEmpty()) {
            lock(false);
        }

        if (collected.affectsCost()) {
            profile.getBank().deposit(adjustOnBuy ? getCost() * 2 + 1 : getCost() * 2);
            if (adjustOnBuy) {
                this.context.setCost(getCost() * 2 + 1);
                adjustOnBuy = false;
            } else {
                this.context.setCost(getCost() * 2);
            }
        } else {
            profile.getBank().deposit(1);
        }
        collecteds.forEach(c -> c.getCollectable().setCost(getCost()));
    }

    /**
     * Inflates the cost of the {@link Collectable} for the specified {@link UserProfile} by the specified amount.
     * 
     * @param profile the {@code UserProfile} to inflate the cost for.
     * @param cost the amount to inflate the cost by.
     * @throws InsufficientCoinsException if the {@code UserProfile} does not have enough coins.
     */
    public void inflateCost(UserProfile profile, long cost) throws InsufficientCoinsException {
        this.context.setCost(getCost() + (cost / (collecteds.size() * 2)));
        collecteds.forEach(collected -> collected.getCollectable().setCost(getCost()));
        profile.getBank().withdrawl(cost);
    }

    /**
     * Returns the number of {@link Collected Collecteds} of the {@link Collectable}.
     * 
     * @return the number of {@code Collecteds} of the {@code Collectable}.
     */
    public int numCards() {
        return collecteds.size();
    }

    /**
     * Sets the {@link CollectableContext} of the {@link Collectable}.
     * 
     * @param context the {@code CollectableContext} of the {@code Collectable}.
     */
    public void setContext(CollectableContext context) {
        this.context = context;
    }

    /**
     * Returns the {@link Rarity} of the {@link Collectable} for the specified {@link UserProfileManager} and {@link CollectableManager}.
     * 
     * @param manager the {@code UserProfileManager} to get the total coins from.
     * @param collectableManager the {@code CollectableManager} to get the total coins from.
     * @return the {@code Rarity} of the {@code Collectable} for the specified {@code UserProfileManager} and {@code CollectableManager}.
     */
    public Rarity getRarity(UserProfileManager manager, CollectableManager collectableManager) {
        long totalCoins = manager.getTotalCoins() + collectableManager.getTotalCoins();
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

    /**
     * Returns the {@link Color} of the {@link Collectable} for the specified {@link Rarity}.
     * 
     * @param rarity the {@code Rarity} to get the {@code Color} for.
     * @return the {@code Color} of the {@code Collectable} for the specified {@code Rarity}.
     */
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

    /**
     * Returns the id of the {@link UserProfile} that created the {@link Collectable}.
     * 
     * @return the id of the {@code UserProfile} that created the {@code Collectable}.
     */
    public String getCreatorId() {
        return context.getOwnerId();
    }

    /**
     * Returns whether or not the {@link Collectable} is owned by the specified {@link UserProfile}.
     * 
     * @param profile the {@code UserProfile} to check.
     * @return {@code true} if the {@code Collectable} is owned by the {@code UserProfile}, {@code false} otherwise.
     */
    public boolean owns(UserProfile profile) {
        return profile.getCollecteds().stream().anyMatch(collected -> collected.getCollectable().getName().equals(context.getName()));
    }

    /**
     * Returns all {@link Collected Collecteds} of the {@link Collectable} owned by the specified {@link UserProfile}.
     * 
     * @param profile the {@code UserProfile} to get the {@code Collecteds} for.
     * @return a list of all {@code Collecteds} of the {@code Collectable} owned by the {@code UserProfile}.
     */
    public List<Collected> getUserCollecteds(UserProfile profile) {
        return collecteds.stream().filter(c -> c.getMemberId().equals(profile.getMemberId())).toList();
    }

    /**
     * Updates the {@link Collectable} with the specified {@link Collected}.
     * 
     * @param collected the {@code Collected} to update the {@code Collectable} with.
     */
    public void updateCollected(Collected collected) {
        collecteds.removeIf(c -> c.getId().equals(collected.getId()));
        collecteds.add(collected);
    }

    /**
     * Returns the url of the edition icon.
     * 
     * @return the url of the edition icon.
     */
    public static String editionIconUrl() {
        return EDITION_ICON_URL;
    }

    /**
     * Returns the next edition of the {@link Collectable}.
     * 
     * @return the next edition of the {@code Collectable}.
     */
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

    /**
     * Returns the highest edition of the {@link Collected Collecteds} of this {@link Collectable}.
     * 
     * @return the highest edition of the {@link Collected Collecteds} of this {@link Collectable}.
     */
    public int getHighestEdition() {
        int highestEdition = 0;
    
        for (Collected c : collecteds) {
            if (c.getEdition() > highestEdition) {
                highestEdition = c.getEdition();
            }
        }
    
        return highestEdition;
    }

    /**
     * Returns the maximum edition of the {@link Collected Collecteds} of this {@link Collectable}.
     * 
     * @return the maximum edition of the {@link Collected Collecteds} of this {@link Collectable}.
     */
    public int getMaxEdition() {
        int numCards = collecteds.size();
        int highestEdition = getHighestEdition();

        return Math.max(numCards, highestEdition);
    }

    /**
     * Returns the general {@link EmbedCreateSpec} for the {@link Collectable}.
     * 
     * @param userManager the {@code UserProfileManager} to get the {@code UserProfile} from.
     * @param collectableManager the {@code CollectableManager} to get the {@code Collectable} from.
     * @return the general {@code EmbedCreateSpec} for the {@code Collectable}.
     */
    public EmbedCreateSpec getGeneralEmbed(UserProfileManager userManager, CollectableManager collectableManager) {
        GuildInteractionHandler guildHandler = new GuildInteractionHandler(userManager.getGuild());
        return EmbedCreateSpec.builder()
            .title(this.getName())
            .description(this.toString() + "\nRarity: ***" + this.getRarity(userManager, collectableManager).toString() + "***\nCreated by: " + guildHandler.getMemberById(this.getCreatorId()).getMention())
            .image(this.getImageUrl())
            .color(Collectable.getRarityColor(this.getRarity(userManager, collectableManager)))
            .build();
    }

    /**
     * Returns a String representation of the {@link Collectable}.
     * 
     * @return a String representation of the {@code Collectable}.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("*" + getDescription() + "*\n\n");
        sb.append("Cost: " + "*" + getCost() + "*\n");
        sb.append("Initial Cost: " + "*" + getInitialCost() + "*\n");
        if (isLocked()) {
            sb.append("*Unavailable to purchase*");
        } else {
            sb.append("*Available to purchase*");
        }        
        return sb.toString();
    }

}
