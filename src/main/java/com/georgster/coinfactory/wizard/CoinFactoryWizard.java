package com.georgster.coinfactory.wizard;

import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfactory.model.CoinFactory;
import com.georgster.coinfactory.model.upgrades.FactoryUpgrade;
import com.georgster.coinfactory.model.upgrades.tracks.FactoryUpgradeTrack;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.economy.exception.InsufficientCoinsException;
import com.georgster.profile.UserProfile;
import com.georgster.settings.UserSettings;
import com.georgster.util.DateTimed;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * An {@link InputWizard} for interacting with a Member's {@link CoinFactory}.
 */
public final class CoinFactoryWizard extends InputWizard {

    private final CoinFactory factory; // the factory of the user
    private final UserProfileManager manager; // manager which controls the user's profile (factory stored within profile)
    private final UserSettings userSettings; // the user's settings (mostly for time displays)
    private final UserProfile profile; // the user's profile (for convenience to update manager)
    
    /**
     * Creates a new {@link CoinFactoryWizard} for the given {@link CommandExecutionEvent}.
     * 
     * @param event the {@link CommandExecutionEvent} to create the wizard for.
     */
    public CoinFactoryWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Coin Factory").builder().withTimeoutDuration(120000).build());
        this.manager = event.getUserProfileManager();
        this.userSettings = event.getClientContext().getUserSettingsManager().get(event.getDiscordEvent().getUser().getId().asString());
        this.profile = manager.get(event.getDiscordEvent().getAuthorAsMember().getId().asString());
        this.factory = profile.getFactory();
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("factoryHome");
        end();
    }

    /**
     * The home page for the {@link CoinFactoryWizard}.
     * <p>
     * This window displays the options for the user to interact with their {@link CoinFactory} and displays the current state of the factory
     * as defined by the embed returned by {@link CoinFactory#getDetailEmbed(UserProfileManager)}.
     */
    public void factoryHome() {

        withResponse(response -> {
            if (response.equals("view upgrade tracks")) {
                nextWindow("viewUpgradeTracks", 0);
            } else if (response.equals("view investment")) {
                nextWindow("viewCoinInvestment");
            } else if (response.equals("manage upgrade order")) {
                nextWindow("switchUpgrades");
            } else if (response.equals("prestige")) {
                if (!factory.canBePrestiged()) {
                    try {
                        factory.prestige();
                    } catch (Exception e) {
                        sendMessage("*" + e.getMessage() + "*", "Coin Factory Cannot Be Prestiged");
                    }
                } else {
                    nextWindow("prestigeFactory");
                }
            }
        }, false, factory.getDetailEmbed(manager, userSettings), "View Upgrade Tracks", "Manage Upgrade Order", "View Investment", "Prestige");
    }

    /**
     * Displays the {@link FactoryUpgradeTrack Upgrade Tracks} for the factory based on the starting position of the current window.
     * <p>
     * This window will display at most 2 {@link FactoryUpgradeTrack Upgrade Tracks} at a time.
     * 
     * @param startingPos the starting position of the current window in the {@link FactoryUpgradeTrack Upgrade Tracks} list.
     */
    public void viewUpgradeTracks(Integer startingPos) {
        List<FactoryUpgradeTrack> tracks = factory.getCurrentUpgradeTracks();
        int endPos = Math.min(startingPos + 2, tracks.size());
        // get a sublist starting from the starting position and ending at the end position
        List<FactoryUpgradeTrack> displayedTracks = tracks.subList(startingPos, endPos);

        List<String> options = new ArrayList<>(displayedTracks.stream().map(FactoryUpgradeTrack::getName).toList());

        // add "next" option if there are more tracks to display
        if (endPos < tracks.size()) {
            options.add("Next");
        }
        // add "back" option if the starting position is greater than 1
        if (startingPos > 1) {
            options.add("Back");
        }
        // always adds "home" option
        options.add("!Home");

        StringBuilder prompt = new StringBuilder("Available Upgrade Tracks:\n\n");

        for (int i = 0; i < displayedTracks.size(); i++) {
            FactoryUpgradeTrack track = displayedTracks.get(i);
            prompt.append(i + 1 + startingPos).append(". ").append(track.getName()).append("\n");
            prompt.append("\t- *").append(track.getTag()).append("*\n");
            // iterate through the upgrades in the track and display the first one that returns false when "isOwned()" is called, or "MAX" if all are owned
            FactoryUpgrade upgrade = track.getCurrentUpgrade();
            if (track.isMaxUpgrade(upgrade.getName())) {
                prompt.append("\t\t- ***MAX***\n");
            } else {
                prompt.append("\t\t- **").append(upgrade.getName()).append("** *(Level ").append(upgrade.getLevel()).append(")*\n");
            }
        }

        prompt.append("\n*Select an upgrade track to view more details, use next/back to iterate through them, or press 'Home' to return to the homepage.*");

        withResponse(response -> {
            if (response.equals("next")) {
                nextWindow("viewUpgradeTracks", endPos);
            } else if (response.equals("back")) {
                nextWindow("viewUpgradeTracks", startingPos - 2); // Should never go negative because back option only available if startingPos > 1
            } else if (response.equals("home")) {
                nextWindow("factoryHome"); // Returns to the homepage
            } else {
                // Get the upgrade track that has the same name as the response
                FactoryUpgradeTrack track = displayedTracks.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(response))
                    .findFirst()
                    .orElse(null);
                if (track != null) {
                    nextWindow("viewUpgradeTrack", track);
                }
            }
        }, false, prompt.toString(), options.toArray(new String[options.size()]));
    }

    /**
     * Displays the given {@link FactoryUpgradeTrack}.
     * 
     * @param track the {@link FactoryUpgradeTrack} to display.
     */
    public void viewUpgradeTrack(FactoryUpgradeTrack track) {
        FactoryUpgrade currentUpgrade = track.getCurrentUpgrade();
        FactoryUpgrade nextUpgrade = track.getNextUpgrade(currentUpgrade);
        StringBuilder prompt = new StringBuilder("Track: ***" + track.getName() + "***\n");
        prompt.append("- *" + track.getTag() + "*\n\n");
        prompt.append("Current Upgrade: **" + currentUpgrade.getName() + "**\n"
                + "- *" + currentUpgrade.getDescription() + "*\n\n");
        List<String> options = new ArrayList<>();
        
        if (track.isMaxUpgrade(currentUpgrade.getName())) {
            prompt.append("**MAX UPGRADE**\n");
            prompt.append("- *This upgrade track is at its maximum level.*\n\n");
        } else {
            options.add("Purchase " + nextUpgrade.getName());
            prompt.append("Next Upgrade: **").append(nextUpgrade.getName()).append("** *(").append(nextUpgrade.getCost(factory.getPrestige())).append(" coins)*\n");
            prompt.append("- *").append(nextUpgrade.getDescription()).append("*\n\n");
        } 
        if (track.ownsAny()) {
            options.add("Refund " + currentUpgrade.getName());
            prompt.append("*You may refund **").append(currentUpgrade.getName()).append("** for ").append(currentUpgrade.getRefundValue(factory.getPrestige())).append(" coins.*\n");
        }

        withResponse(response -> {
            if (response.startsWith("purchase")) {
                nextWindow("purchaseUpgrade", track, nextUpgrade.getName());
            } else if (response.startsWith("refund")) {
                nextWindow("refundUpgrade", track, currentUpgrade.getName());
            }
        }, true, prompt.toString(), options.toArray(new String[options.size()]));
    }

    /**
     * The window to purchase a specific {@link FactoryUpgrade} from a {@link FactoryUpgradeTrack}.
     * 
     * @param track the {@link FactoryUpgradeTrack} to purchase the upgrade from.
     * @param upgradeName the name of the upgrade to purchase.
     */
    public void purchaseUpgrade(FactoryUpgradeTrack track, String upgradeName) {
        FactoryUpgrade upgrade = track.getUpgrade(upgradeName);
        StringBuilder prompt = new StringBuilder();
        prompt.append("**" + upgrade.getName() + "** *(Level " + upgrade.getLevel() + " Upgrade)*\n");
        prompt.append("- *" + upgrade.getDescription() + "*\n\n");
        prompt.append("Your factory currently has **" + factory.getInvestedCoins() + "** coins. Would you like to buy " + upgrade.getName() + " for **" + upgrade.getCost(factory.getPrestige()) + "** coins?\n");
        prompt.append("*You may refund this upgrade for " + upgrade.getRefundValue(factory.getPrestige()) + " coins.*");

        withResponse(response -> {
            if (response.equals("purchase upgrade")) {
                try {
                    factory.purchaseUpgrade(upgrade);
                    manager.update(profile);
                    sendMessage("You have successfully purchased **" + upgrade.getName() + "** for **" + upgrade.getCost(factory.getPrestige()) + "** coins." +
                                "\n*Your CoinFactory now has* **" + factory.getInvestedCoins() + "** *coins invested.*" +
                                "\n\n*Upgrade track* ***" + track.getName() + "*** *is now at level* **" + upgrade.getLevel() + "**", "Upgrade Purchased");
                                goBack();
                } catch (InsufficientCoinsException e) {
                    sendMessage("You currently have **" + factory.getInvestedCoins() + "** coins invested in your CoinFactory.\n" +
                                "You need **" + upgrade.getCost(factory.getPrestige()) + "** coins to purchase **" + upgrade.getName() + "**", "Insufficient Coins");
                }
            } else if (response.equals("coin investment")) {
                nextWindow("viewCoinInvestment");   
            }
        }, true, prompt.toString(), "Purchase Upgrade", "Coin Investment");
    }

    /**
     * The window to refund a specific {@link FactoryUpgrade} from a {@link FactoryUpgradeTrack}.
     * 
     * @param track the {@link FactoryUpgradeTrack} to refund the upgrade from.
     * @param upgradeName the name of the upgrade to refund.
     */
    public void refundUpgrade(FactoryUpgradeTrack track, String upgradeName) {
        FactoryUpgrade upgrade = track.getUpgrade(upgradeName);
        StringBuilder prompt = new StringBuilder();
        prompt.append("**" + upgrade.getName() + "** *(Level " + upgrade.getLevel() + " Upgrade)*\n");
        prompt.append("- *" + upgrade.getDescription() + "*\n\n");
        prompt.append("Would you like to refund " + upgrade.getName() + " for **" + upgrade.getRefundValue(factory.getPrestige()) + "** coins?\n");

        withResponse(response -> {
            if (response.equals("refund upgrade")) {
                factory.refundUpgrade(upgrade);
                manager.update(profile);
                sendMessage("You have successfully refunded **" + upgrade.getName() + "** for **" + upgrade.getRefundValue(factory.getPrestige()) + "** coins." +
                            "\n*Your CoinFactory now has* **" + factory.getInvestedCoins() + "** *available coins invested.*" +
                            "\n\n*Upgrade track* ***" + track.getName() + "*** *is now at level* **" + (upgrade.getLevel() - 1) + "**", "Upgrade Refunded");
                goBack();
            }
        }, true, prompt.toString(), "Refund Upgrade");
    }

    /**
     * Displays the current coin investment in the {@link CoinFactory}.
     */
    public void viewCoinInvestment() {
        StringBuilder prompt = new StringBuilder("**Coin Investment**\n\n");
        prompt.append("**Factory:** ***" + factory.getInvestedCoins() + "*** coins.\n");
        prompt.append("**Coin Bank:** ***" + profile.getBank().getBalance() + "*** coins.\n");
        prompt.append("**Production Rate:** ***" + factory.getProductionRateValue() + "*** coins per process cycle.\n\n");
        DateTimed nextProcessTime = manager.getNextFactoryProcessTime();
        prompt.append("This Factory will process coins next at *" + nextProcessTime.getFormattedTime(userSettings) + "* on *" + nextProcessTime.getFormattedDate(userSettings) + "*.\n\n");
        prompt.append("What would you like to do?");

        withResponse(response -> {
            if (response.equals("invest coins")) {
                nextWindow("investCoins");
            } else if (response.equals("withdraw coins")) {
                nextWindow("withdrawCoins");
            }
        }, true, prompt.toString(), "Invest Coins", "Withdraw Coins");
    }

    /**
     * Allows the user to invest coins in the {@link CoinFactory}.
     */
    public void investCoins() {
        StringBuilder prompt = new StringBuilder("**Invest Coins**\n\n");
        prompt.append("**Factory:** ***" + factory.getInvestedCoins() + "*** coins.\n");
        prompt.append("**Coin Bank:** ***" + profile.getBank().getBalance() + "*** coins.\n\n");
        prompt.append("Please type how many coins would you like to invest in your Coin Factory from your Coin Bank.\n");
        prompt.append("*You may withdraw these coins at any time.*");

        withResponse(response -> {
            try {
                long coins = Long.parseLong(response);
                factory.deposit(coins, profile.getBank());
                manager.update(profile);
                sendMessage("You have successfully invested **" + coins + "** coins in your Coin Factory." +
                            "\n*Your CoinFactory now has* **" + factory.getInvestedCoins() + "** *coins invested.*", "Coins Invested");
                goBack();
            } catch (InsufficientCoinsException e) {
                sendMessage(e.getMessage(), "Insufficient Coins");
            } catch (NumberFormatException e) {
                sendMessage("You must enter a valid number.", "Invalid Number");
            }
        }, true, prompt.toString());
    }

    /**
     * Allows the user to withdraw coins from the {@link CoinFactory}.
     */
    public void withdrawCoins() {
        StringBuilder prompt = new StringBuilder("**Withdraw Coins**\n\n");
        prompt.append("**Factory:** ***" + factory.getInvestedCoins() + "*** coins.\n");
        prompt.append("**Coin Bank:** ***" + profile.getBank().getBalance() + "*** coins.\n\n");
        prompt.append("Please type how many coins would you like to withdraw from your Coin Factory to your Coin Bank.\n");
        prompt.append("*You may only withdraw coins that have been processed.*");

        withResponse(response -> {
            try {
                long coins = Long.parseLong(response);
                factory.withdraw(coins, profile.getBank());
                manager.update(profile);
                sendMessage("You have successfully withdrawn **" + coins + "** coins from your Coin Factory." +
                            "\n*Your CoinFactory now has* **" + factory.getInvestedCoins() + "** *coins invested.*", "Coins Withdrawn");
                goBack();
            } catch (InsufficientCoinsException e) {
                sendMessage(e.getMessage(), "Insufficient Coins");
            } catch (NumberFormatException e) {
                sendMessage("You must enter a valid number.", "Invalid Number");
            }
        }, true, prompt.toString());
    }

    /**
     * Allows the user to manage the process order of the {@link FactoryUpgrade FactoryUpgrades} in the {@link CoinFactory}.
     */
    public void switchUpgrades() {
        StringBuilder prompt = new StringBuilder("**Upgrade Process Reordering**\n\n");
        prompt.append("Your upgrade process order will currently produce **" + factory.getProductionRateValue() + "** coins per production cycle.\n");
        prompt.append("Current upgrade order is:\n");

        List<FactoryUpgrade> upgrades = factory.getUpgrades();
        String[] options = new String[upgrades.size()];

        for (int i = 0; i < upgrades.size(); i++) {
            FactoryUpgrade upgrade = upgrades.get(i);
            prompt.append("- ").append(i + 1).append(". ").append(upgrade.getName()).append(" *(Level ").append(upgrade.getLevel()).append(")*\n");
            options[i] = upgrade.getName();
        }

        prompt.append("Which upgrade would you like to move?\n\n");
        prompt.append("*The order in which your upgrades are processed can affect how many coins are produced during a coin production cycle*");

        withResponse(response -> {
            nextWindow("switchUpgrade", response);
        }, true, prompt.toString(), options);
    }

    /**
     * Allows the user to switch the position of a specific {@link FactoryUpgrade} in the process order of the {@link CoinFactory}.
     * 
     * @param upgradeName the name of the upgrade to switch.
     */
    public void switchUpgrade(String upgradeName) {
        List<FactoryUpgrade> upgrades = factory.getUpgrades();
        FactoryUpgrade upgrade = factory.getUpgrade(upgradeName);
        int currentPosition = upgrades.indexOf(upgrade);

        StringBuilder prompt = new StringBuilder("**Switching** ***" + upgrade.getName() + "***\n");
        prompt.append("- *" + upgrade.getDescription() + "*\n\n");
        prompt.append("Your upgrade process order will currently produce **" + factory.getProductionRateValue() + "** coins per production cycle.\n");
        prompt.append("**" + upgrade.getName()).append("'s** Current Position: *").append(currentPosition + 1).append("*\n");

        for (int i = 0; i < upgrades.size(); i++) {
            FactoryUpgrade displayedUpgrade = upgrades.get(i);
            prompt.append("- ").append(i + 1).append(". ");
            if (displayedUpgrade.getName().equals(upgrade.getName())) {
                prompt.append("***" + displayedUpgrade.getName() + "***");
            } else {
                prompt.append(displayedUpgrade.getName());
            }
            prompt.append(" *(Level ").append(displayedUpgrade.getLevel()).append(")*\n");
        }

        String[] options = new String[0];

        if (currentPosition == 0 && currentPosition < (upgrades.size() - 1)) {
            options = new String[] {"v"};
            prompt.append("\n*You may move this upgrade down in the process order list*");
        } else if (currentPosition == (upgrades.size() - 1) && currentPosition > 0) {
            options = new String[] {"^"};
            prompt.append("\n*You may move this upgrade up in the process order list*");
        } else if (upgrades.size() > 1) { // if the other conditions fail and there is more than one upgrade, it falls somewhere in the middle and can be moved up or down
            options = new String[] {"v", "^"};
            prompt.append("\n*You may move this upgrade up or down in the process order list*");
        } else {
            prompt.append("\n*This upgrade cannot be moved in the process order list*");
        }

        withResponse(response -> {
            if (response.equals("v")) {
                factory.swap(upgrade, currentPosition + 1);
                manager.update(profile);
            } else if (response.equals("^")) {
                factory.swap(upgrade, currentPosition - 1);
            }
        }, true, prompt.toString(), options);
    }

    /**
     * Allows the user to prestige the {@link CoinFactory}.
     */
    public void prestigeFactory() {
        StringBuilder prompt = new StringBuilder("**Prestige Coin Factory**\n\n");
        prompt.append("Prestiging your coin factory will cost **" + factory.getPrestigeCost() + "** coins and reset all owned upgrades.\n");
        prompt.append("You will receive a prestige point and a bonus to your production rate, but upgrades will cost more\n\n");
        prompt.append("*The color of your Coin Factory may change to signify the prestige*\n\n");
        prompt.append("***Are you sure you want to prestige your Coin Factory?***");
        prompt.append("*This cannot be undone.*");

        withResponse(response -> {
            if (response.equals("prestige factory")) {
                //factory.prestige();
                //manager.update(profile);
                sendMessage("You have successfully prestiged your Coin Factory to **Level " + factory.getPrestige() + "**\n" +
                            "You have received a prestige point and a bonus to your production rate.\n\n" +
                            "*All upgrades have been reset and the prestige cost has been withdrawn.*", "Coin Factory Prestiged");
                goBack();
            }
        }, true, prompt.toString(), "Prestige Factory");
    }
}
