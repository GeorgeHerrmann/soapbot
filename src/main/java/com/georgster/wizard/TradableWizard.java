package com.georgster.wizard;

import java.util.ArrayList;
import java.util.List;

import com.georgster.collectable.trade.TradeOffer;
import com.georgster.collectable.trade.Tradeable;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.TradableWizard.PersonalTradeWizard.TradeResult;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

public final class TradableWizard extends InputWizard {
    private final UserProfile profile1;
    private final UserProfile profile2;
    private final UserProfileManager manager;
    private List<Tradeable> tradeables;

    private final TradeOffer offer;

    public class PersonalTradeWizard extends InputWizard {
        private final TradeOffer offer;
        private TradeResult result;

        public enum TradeResult {
            ACCEPTED, DECLINED, TIMEOUT
        }

        protected PersonalTradeWizard(CommandExecutionEvent event, TradeOffer offer) {
            super(event, InputListenerFactory.createButtonMessageListener(event, "Trade Offer From " + offer.getOfferer().getUsername()).builder().withTimeoutDuration(300000).allowAllResponses(true).build());
            this.offer = offer;
            this.result = TradeResult.TIMEOUT;
            swtichToUserWizard(event.getGuildInteractionHandler().getMemberById(offer.getReciever().getId()));
        }

        public void begin() {
            nextWindow("presentTrade");
        }

        protected void presentTrade() {
            StringBuilder prompt = new StringBuilder("You have recieved a trade offer from " + offer.getOfferer().getUsername() + ":\n\n");

            if (!offer.getOfferedItems().isEmpty()) {
                prompt.append("Offered Items:");
                offer.getOfferedItems().forEach(item -> prompt.append("\n").append(item.getName() + " - ID: " + item.getId()));
            } else {
                prompt.append("No Items Offered");
            }
            if (offer.getOfferedCoins() > 0) {
                prompt.append("\nOffered Coins: ").append(offer.getOfferedCoins());
            } else {
                prompt.append("\nNo Coins Offered");
            }
            if (!offer.getRequestedItems().isEmpty()) {
                prompt.append("\n\nRequested Items:");
                offer.getRequestedItems().forEach(item -> prompt.append("\n").append(item.getName() + " - ID: " + item.getId()));
            } else {
                prompt.append("\n\nNo Items Requested");
            }
            if (offer.getRequestedCoins() > 0) {
                prompt.append("\nRequested Coins: ").append(offer.getRequestedCoins());
            } else {
                prompt.append("\nNo Coins Requested");
            }

            prompt.append("\n\nWould you like to accept this trade?");

            withResponse(response -> {
                if (response.equals("accept")) {
                    result = TradeResult.ACCEPTED;
                    getInputListener().editCurrentMessageContent("Offer Accepted!");
                    shutdown();
                } else {
                    result = TradeResult.DECLINED;
                    getInputListener().editCurrentMessageContent("Offer Declined!");
                    shutdown();
                }
            }, false, prompt.toString(), "Accept", "!Decline");
        }

        protected TradeResult getResult() {
            return result;
        }
    }

    public TradableWizard(CommandExecutionEvent event, UserProfile profile1, UserProfile profile2) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "Trade Wizard").builder().requireMatch(false, false).disableAutoFormatting().build());
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.manager = event.getUserProfileManager();
        this.offer = new TradeOffer(profile1, profile2);
        this.tradeables = new ArrayList<>(profile1.getAllTradeables());
    }

    public void begin() {
        nextWindow("offerTradeables");
        end();
    }

    protected void offerTradeables() {
        final InputListener newListener = InputListenerFactory.createMenuMessageListener(event, "Add Items To Trade");

        StringBuilder sb = new StringBuilder("Which items would you like to offer in this trade? Select \"Complete\" when you are done.");
        if (!offer.getOfferedItems().isEmpty()) {
            sb.append("\n\nOffered Items:");
            offer.getOfferedItems().forEach(item -> sb.append("\n").append(item.getName() + " - ID: " + item.getId()));
        }
        String[] options = new String[tradeables.size() + 1];
        for (int i = 0; i < tradeables.size(); i++) {
            options[i] = tradeables.get(i).getName() + " - ID: " + tradeables.get(i).getId();
        }
        options[tradeables.size()] = "Complete";

        withResponse(response -> {
            if (response.equals("complete")) {
                nextWindow("offerCoins");
            } else {
                String id = response.substring(response.indexOf("id: ") + 4);
                Tradeable item = profile1.getTradeableById(id);
                offer.addOfferedItem(item);
                tradeables.remove(item);
            }
        }, false, newListener, sb.toString(), options);
    }

    protected void offerCoins() {
        final String prompt = "Please type how many coins you would like to offer in this trade, or select \"Continue\" to not offer coins";

        withResponse(response -> {
            try {
                long coins = 0;
                if (!response.equals("continue")) {
                    coins = Long.parseLong(response);
                }
                if (coins < 0) {
                    sendMessage("Sorry, you must offer at least **0** coins in a trade.", "Invalid Amount");
                } else if (profile1.getBank().hasBalance(coins)) {
                    offer.setOfferedCoins(coins);
                    this.tradeables = new ArrayList<>(profile2.getAllTradeables());
                    nextWindow("requestTradeables");
                } else {
                    sendMessage("Sorry, you can not offer " + response + " coins when you only have " + profile1.getBank().getBalance() + " coins.", "Invalid Amount");
                }
            } catch (NumberFormatException e) {
                sendMessage(response + " is not a valid amount of coins, please try again", "Invalid input");
            }
        }, true, prompt, "Continue");
    }

    protected void requestTradeables() {
        final InputListener newListener = InputListenerFactory.createMenuMessageListener(event, "Add Items To Trade");

        StringBuilder sb = new StringBuilder("Which items would you like to request in this trade? Select \"Complete\" when you are done.");
        if (!offer.getRequestedItems().isEmpty()) {
            sb.append("\n\nRequested Items:");
            offer.getRequestedItems().forEach(item -> sb.append("\n").append(item.getName() + " - ID: " + item.getId()));
        }
        String[] options = new String[tradeables.size() + 1];
        for (int i = 0; i < tradeables.size(); i++) {
            options[i] = tradeables.get(i).getName() + " - ID: " + tradeables.get(i).getId();
        }
        options[tradeables.size()] = "Complete";

        withResponse(response -> {
            if (response.equals("complete")) {
                nextWindow("requestCoins");
            } else {
                String id = response.substring(response.indexOf("id: ") + 4);
                Tradeable item = profile2.getTradeableById(id);
                offer.addRequestedItem(item);
                tradeables.remove(item);
            }
        }, false, newListener, sb.toString(), options);
    }

    protected void requestCoins() {
        final String prompt = "Please type how many coins you would like to request in this trade, or select \"Continue\" to not request coins";

        withResponse(response -> {
            try {
                long coins = Long.parseLong(response);
                if (coins < 0) {
                    sendMessage("Sorry, you must request at least **0** coins in a trade.", "Invalid Amount");
                } else if (profile2.getBank().hasBalance(coins)) {
                    offer.setRequestedCoins(coins);
                    sendTradeOffer();
                    end();
                } else {
                    sendMessage("Sorry, you can not request " + response + " coins when you only have " + profile2.getBank().getBalance() + " coins.", "Invalid Amount");
                }
            } catch (NumberFormatException e) {
                sendMessage(response + " is not a valid amount of coins, please try again", "Invalid input");
            }
        }, true, prompt, "Continue");
    }

    protected void sendTradeOffer() {
        ThreadPoolFactory.scheduleGeneralTask(getGuild().getId().asString(), () -> {
            PersonalTradeWizard wizard = new PersonalTradeWizard(event, offer);
            event.getUserInteractionHandler().sendMessage("I have sent a trade offer to " + profile2.getUsername() + "\nThey have 5 minutes to response. I will send you a message with the result.", "Trade Offer Sent");
            wizard.begin();
            TradeResult result = wizard.getResult();

            if (result == TradeResult.ACCEPTED) {
                event.getUserInteractionHandler().sendMessage("Your trade offer was accepted by " + profile2.getUsername(), "Trade Offer Accepted");
                offer.executeTrade();
                manager.update(profile1);
                manager.update(profile2);
                event.getCollectableManager().updateFromProfiles(manager);
            } else if (result == TradeResult.DECLINED) {
                event.getUserInteractionHandler().sendMessage("Your trade offer was declined by " + profile2.getUsername(), "Trade Offer Declined");
            } else {
                event.getUserInteractionHandler().sendMessage("Your trade offer was not accepted by " + profile2.getUsername() + " in time", "Trade Offer Timeout");
            }
        });
    }
}
