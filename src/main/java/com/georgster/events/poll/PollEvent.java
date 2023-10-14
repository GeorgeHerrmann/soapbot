package com.georgster.events.poll;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.TimedEvent;
import com.georgster.util.SoapUtility;
import com.georgster.util.handler.GuildInteractionHandler;

/**
 * A {@link SoapEvent} that has a prompt and a List of options users can vote for.
 * A {@code PollEvent} uses {@link TimedEvent}, and the poll will conclude once the TimedEvent ends.
 * <p>
 * A user can only vote for one option at a time, and the event's owner is the one who created the poll.
 * By default, a {@code PollEvent} will last for 5 minutes upon creation.
 * <p>
 * This event's identifier is the prompt of the poll.
 */
public class PollEvent extends TimedEvent implements SoapEvent {
    private static final SoapEventType TYPE = SoapEventType.POLL;

    private String identifier;
    private Map<String, List<String>> options; // Option and the voters
    private String channel;
    private String owner;

    
    /**
     * Creates a PollEvent that will expire in 5 minutes.
     * Generally used when initially created by the {@code PollEventWizard}.
     * 
     * @param identifier The prompt for the event.
     * @param channel The channel the event was created in.
     * @param owner The creator of the event.
     */
    public PollEvent(String identifier, String channel, String owner) {
        super(getexpirationTime());

        this.identifier = identifier;
        this.channel = channel;
        this.owner = owner;
        this.options = new TreeMap<>();
    }

    /**
     * Creates a PollEvent with a descriptive time and date.
     * Generally used when a PollEvent is pulled from the database.
     * 
     * @param identifier The prompt for the event.
     * @param channel The channel the event was created in.
     * @param owner The creator of the event.
     * @param options The event's options and voters.
     * @param time The event's time.
     * @param date The event's date.
     */
    public PollEvent(String identifier, String channel, String owner, Map<String, List<String>> options, String time, String date) {
        super (time, date);

        this.identifier = identifier;
        this.channel = channel;
        this.owner = owner;
        this.options = options;
    }

    /**
     * Returns the default time until a poll event will expire from the current time.
     * 
     * @return The default time until a poll event will expire from the current time.
     */
    private static String getexpirationTime() {
        LocalTime now = LocalTime.now(ZoneId.of("-05:00")).plusHours(1);
        return now.plusMinutes(5).toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean fulfilled() {
        return until() <= 0;
    }

    /**
     * {@inheritDoc}
     */
    public void onFulfill(GuildInteractionHandler handler) {
        StringBuilder sb = new StringBuilder("Poll " + identifier + " has concluded! The votes are as follows:\n");
        Map<String, Integer> voteTally = getVoteTally();
        voteTally.forEach((option, votes) -> sb.append("- " + option + ": " + votes + " votes\n"));

        handler.sendMessage(sb.toString(), "Poll " + getIdentifier() + " concluded!");
    }

    /**
     * Returns if a member has already voted for this poll.
     * 
     * @param voter The tag of the member.
     * @return true if they already voted, false otherwise.
     */
    public boolean alreadyVoted(String voter) {
        for (List<String> voters : options.values()) {
            if (voters.contains(voter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if a member has voted for the specified option.
     * 
     * @param option The poll option to check for.
     * @param voter The voter to check for.
     * @return True if they voted for the specified option, false otherwise.
     */
    public boolean votedFor(String option, String voter) {
        List<String> voters = options.get(option);

        return voters != null && voters.contains(voter);
    }

    /**
     * Returns a List containing all the options of this event.
     * 
     * @return A List containing all the options of this event.
     */
    public List<String> getOptions() {
        List<String> optionList = new ArrayList<>();
        options.forEach((option, voters) -> optionList.add(option));
        return optionList;
    }

    /**
     * Adds an option to this event if it already doesn't exist.
     * 
     * @param option The option to add.
     */
    public void addOption(String option) {
        options.computeIfAbsent(option, (k -> new ArrayList<>()));
    }

    /**
     * Removes an option from this event if it exists.
     * 
     * @param option The option to remove.
     */
    public void removeOption(String option) {
        if (options.containsKey(option)) {
            options.remove(option);
        }
    }

    /**
     * Adds a voter to the given option if they have not already voted for the event.
     * 
     * @param option The option to vote for.
     * @param voter The member who is voting.
     */
    public void addVoter(String option, String voter) {
        if (!alreadyVoted(voter)) {
            options.get(option).add(voter);
        }
    }

    /**
     * Removes a voter from the option they have voted for if they have already voted for the event.
     * 
     * @param voter The member to remove their vote for.
     */
    public void removeVoter(String voter) {
        for (List<String> voters : options.values()) {
            if (voters.contains(voter)) {
                voters.remove(voter);
            }
        }
    }

    /**
     * Returns a Map that shows how many votes each option has gotten.
     * 
     * @return A Map that shows how many votes each option has gotten.
     */
    public Map<String, Integer> getVoteTally() {
        Map<String, Integer> voteMap = new TreeMap<>();

        options.forEach((option, voters) -> voteMap.put(option, voters.size()));

        return voteMap;
    }

    /**
     * Returns true if this poll is a "quick poll", meaning it has only two options
     * which are "yes" and "no", false otherwise.
     * 
     * @return True if this poll is a quick poll, false otherwise.
     */
    public boolean isQuickPoll() {
        List<String> opts = getOptions();
        return opts.size() == 2 && opts.contains("yes") && opts.contains("no");
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    public String getOwner() {
        return owner;
    }

    /**
     * {@inheritDoc}
     */
    public SoapEventType getType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public String getChannel() {
        return channel;
    }

    /**
     * {@inheritDoc}
     */
    public boolean same(SoapEvent compare) {
        if (!(compare instanceof PollEvent)) {
            return false;
        }
        
        return identifier.equals(compare.getIdentifier())
            && channel.equals(compare.getChannel());
    }

    public List<String> generateOptionsList(GuildInteractionHandler handler) {
        List<String> result = new ArrayList<>();

        if (options != null) {
            for (Map.Entry<String, List<String>> entry : options.entrySet()) {
                StringBuilder sb = new StringBuilder();
                String option = entry.getKey();
                List<String> voters = entry.getValue();

                if (option != null && !option.isEmpty()) {
                    sb.append("*" + option + "*\n");
                }

                if (voters != null && !voters.isEmpty()) {
                    for (String voter : voters) {
                        if (voter != null && !voter.isEmpty()) {
                            sb.append("- " + handler.getMemberById(voter).getMention() + "\n");
                        }
                    }
                } else {
                    sb.append("- No votes\n");
                }

                sb.append("Active for another " + SoapUtility.convertSecondsToHoursMinutes((int) until()) + "");
                result.add(sb.toString());
            }
        }

        return result;
    }

    /**
     * Returns a String describing this {@code PollEvent}.
     * 
     * @return A String describing this {@code PollEvent}.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        getVoteTally().forEach(((option, voters) -> sb.append("- " + option + ": " + voters + " votes\n")));
        sb.append("Active for another " + SoapUtility.convertSecondsToHoursMinutes((int) until()) + "");
        return sb.toString();
    }
}
