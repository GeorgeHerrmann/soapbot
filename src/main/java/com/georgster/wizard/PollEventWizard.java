package com.georgster.wizard;

import java.util.List;

import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.poll.PollEvent;
import com.georgster.util.SoapUtility;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.channel.TextChannel;

/**
 * An {@link OldInputWizard} for handling PollEvents.
 */
public class PollEventWizard extends InputWizard {

    private static final String TITLE = "Poll Event Wizard";
    private static final SoapEventType TYPE = SoapEventType.POLL;

    private SoapEventManager eventManager;

    /**
     * Creates a PollEventWizard.
     */
    public PollEventWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.eventManager = event.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        handler.sendText("Welcome to the poll event wizard. At any time you can type \"stop\", or react :x: to exit the wizard", TITLE);
        nextWindow("wizardOptions");
        end();
    }

    /**
     * The main menu for the wizard.
     */
    protected void wizardOptions() {
        String prompt = "What would you like to do?";
        String[] options = {"create a poll", "vote on a poll", "view a poll", "edit a poll"};
        withResponse((response -> {
            if (response.equals("create a poll")) {
                nextWindow("createPoll");
            } else if (response.equals("vote on a poll")) {
                if (!eventManager.hasAny(TYPE)) {
                    sendMessage("No polls currently exist", TITLE);
                } else {
                    nextWindow("pollVotingOptions");
                }
            } else if (response.equals("view a poll")) {
                if (!eventManager.hasAny(TYPE)) {
                    sendMessage("No polls currently exist", TITLE);
                } else {
                    nextWindow("pollViewingOptions");
                }
            } else if (response.equals("edit a poll")) {
                if (!eventManager.hasAny(TYPE)) {
                    sendMessage("No polls currently exist", TITLE);
                } else {
                    nextWindow("pollEditOptions");
                }
            }
        }), false, prompt, options);
    }

    /**
     * Options for viewing a poll.
     */
    protected void pollViewingOptions() {
        String prompt = "Which poll would you like to view?";
        String[] options = new String[eventManager.getCount(TYPE)];
        List<SoapEvent> events = eventManager.getAll(TYPE);
        for (int i = 0; i < eventManager.getCount(TYPE); i++) {
            options[i] = events.get(i).getIdentifier();
        }

        withResponse((response -> {
            PollEvent event = (PollEvent) eventManager.get(response);
            handler.sendText(event.toString(), event.getIdentifier());
        }), true, prompt, options);
    }

    /**
     * Window for selecting a poll to edit.
     */
    protected void pollEditOptions() {
        String prompt = "Which poll would you like to edit?";
        String[] options = new String[eventManager.getCount(TYPE)];
        List<SoapEvent> events = eventManager.getAll(TYPE);
        for (int i = 0; i < eventManager.getCount(TYPE); i++) {
            options[i] = events.get(i).getIdentifier();
        }

        withResponse((response -> {
            PollEvent event = (PollEvent) eventManager.get(response);
            nextWindow("editPoll", event);
        }), true, prompt, options);
    }

    /**
     * Window for selecting how to edit a poll.
     * 
     * @param event The poll being edited.
     */
    protected void editPoll(PollEvent event) {
        String prompt = "What would you like to edit about poll: " + event.getIdentifier() + "?";
        String[] options = {"add option", "remove option", "delete poll"};

        withResponse((response -> {
            if (response.equals("add option")) {
                nextWindow("addOptions", event);
            } else if (response.equals("remove option")) {
                nextWindow("removeOption", event);
            } else if (response.equals("delete poll")) {
                eventManager.remove(event);
                nextWindow("wizardOptions");
            }
        }), true, prompt, options);
    }

    /**
     * Window to remove options from a poll.
     * 
     * @param event The PollEvent being edited.
     */
    protected void removeOption(PollEvent event) {
        String prompt = "Which option would you like to remove from poll: " + event.getIdentifier() + "?";

        List<String> pollOptions = event.getOptions();
        String[] options = pollOptions.toArray(new String[pollOptions.size()]);

        if (event.getOptions().isEmpty()) {
            eventManager.remove(event);
            sendMessage("There are no more options for poll " + event.getIdentifier() + ", it has been removed.", TITLE);
            nextWindow("wizardOptions");
        } else {
            withResponse((response -> {
                event.removeOption(response);
                eventManager.update(event);
                sendMessage("You have removed " + response + " from poll " + event.getIdentifier(), TITLE);
            }), true, prompt, options);
        }
    }

    /**
     * Window for creating the title for a poll.
     */
    protected void createPoll() {
        String prompt = "Please enter the prompt for the poll";

        withResponse((response -> {
            if (response.equals("cancel")) {
                nextWindow("wizardOptions");
            } else {
                if (!eventManager.exists(response, TYPE)) {
                    PollEvent event = new PollEvent(response, ((TextChannel) getChannel()).getName(), getUser().getTag());
                    nextWindow("setExpiration", event);
                } else {
                    sendMessage("A poll with that title already exists, please pick a new name", TITLE);
                }
            }
        }), true, prompt, "cancel");
    }

    /**
     * Window for setting the expiration for a poll.
     * 
     * @param event The PollEvent that is being built.
     */
    protected void setExpiration(PollEvent event) {
        String prompt = "Please type how long the poll should last for, or select use default setting.\n" +
                        "You can type things like: 10 days, 1 hour, 15 minutes";
        String[] options = {"use default setting"};

        withResponse((response -> {
            if (response.equals("use default setting")) {
                event.setDateTime("5 mins");
                nextWindow("addOptions", event);
            } else {
                try {
                    event.setDateTime(response);
                    nextWindow("addOptions", event);
                } catch (IllegalArgumentException e) {
                    sendMessage(e.getMessage(), TITLE);
                }
            }
        }), true, prompt, options);

    }

    /**
     * Window for adding options to a poll.
     * 
     * @param event The PollEvent that is being built.
     */
    protected void addOptions(PollEvent event) {
        String prompt = "Please type the options for the poll, one at a time in their own messages, then click continue when complete.";
        String[] options = {"continue"};

        withResponse((response -> {
            if (response.equals("continue")) {
                if (event.getOptions().isEmpty()) {
                    sendMessage("You must add at least 1 option", TITLE);
                } else {
                    if (eventManager.exists(event, TYPE)) { // If reached by the "edit poll" window
                        eventManager.update(event);
                        handler.sendText("Now has the following options:\n" + event.toString(), "Poll " + event.getIdentifier() + "updated");
                        goBack();
                    } else { // If reached by the "create poll" window
                        eventManager.add(event);
                        StringBuilder sb = new StringBuilder("A new poll " + event.getIdentifier() + " has been created with the following options:\n");
                        event.getOptions().forEach(option -> sb.append("- " + option + "\n"));
                        sb.append("This poll lasts for: " + SoapUtility.convertSecondsToHoursMinutes((int) event.until()) + ". Type !poll to vote!");
                        handler.sendText(sb.toString(), "Poll Created");
                        nextWindow("wizardOptions");
                    }
                }
            } else {
                event.addOption(response);
                sendMessage("Added " + response + " to this event's options.", TITLE);
            }
        }), true, prompt, options);
    }

    /**
     * Window for voting on a poll.
     */
    protected void pollVotingOptions() {
        String prompt = "Which poll would you like to vote on?";
        String[] options = new String[eventManager.getCount(TYPE)];
        List<SoapEvent> events = eventManager.getAll(TYPE);
        for (int i = 0; i < eventManager.getCount(TYPE); i++) {
            options[i] = events.get(i).getIdentifier();
        }

        withResponse((response -> {
            PollEvent event = (PollEvent) eventManager.get(response);
            nextWindow("pollVote", event);
        }), true, prompt, options);
    }

    /**
     * Window for selecting an option for a poll to vote for.
     * 
     * @param event The PollEvent to vote for.
     */
    protected void pollVote(PollEvent event) {
        String voter = getUser().getTag();

        String prompt = "Please select an option to vote for.";
        List<String> options = event.getOptions();
        String[] optionsArr = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            optionsArr[i] = options.get(i);
        }

        withResponse((response -> {
            event.removeVoter(voter);
            event.addVoter(response, voter);
            eventManager.update(event);
            handler.sendText(getUser().getUsername() + " has voted for: " + response + ".\n Current votes are:\n" + event.toString(), event.getIdentifier());
        }), true, prompt, optionsArr);
    }
}
