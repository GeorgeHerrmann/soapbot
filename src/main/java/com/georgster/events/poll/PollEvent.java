package com.georgster.events.poll;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.SoapUtility;

public class PollEvent implements SoapEvent {
    private static final SoapEventType TYPE = SoapEventType.POLL;
    
    private String expiration; // Standardized time String
    private String identifier;
    private Map<String, List<String>> options; // Option and the voters
    private String channel;
    private String owner;

    // Wizard creation
    public PollEvent(String identifier, String channel, String owner) {
        this.identifier = identifier;
        this.channel = channel;
        this.owner = owner;
        this.expiration = getexpirationTime();
        this.options = new TreeMap<>();
    }

    // Database
    public PollEvent(String identifier, String channel, String owner, Map<String, List<String>> options, String expiration) {
        this.identifier = identifier;
        this.channel = channel;
        this.owner = owner;
        this.options = options;
        this.expiration = expiration;
    }

    private String getexpirationTime() {
        LocalTime now = LocalTime.now(ZoneId.of("-05:00")).plusHours(1);
        return now.plusMinutes(5).toString();
    }

    public boolean fulfilled() {
        return until() <= 0;
    }

    public void onFulfill(GuildInteractionHandler handler) {
        StringBuilder sb = new StringBuilder("Poll " + identifier + " has concluded! The votes are as follows:\n");
        Map<String, Integer> voteTally = getVoteTally();
        voteTally.forEach((option, votes) -> sb.append("- " + option + ": " + votes + " votes\n"));

        handler.sendText(sb.toString(), "Poll " + getIdentifier() + " concluded!");
    }

    public boolean alreadyVoted(String voter) {
        for (List<String> voters : options.values()) {
            if (voters.contains(voter)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getOptions() {
        List<String> optionList = new ArrayList<>();
        options.forEach((option, voters) -> optionList.add(option));
        return optionList;
    }

    public void addOption(String option) {
        options.computeIfAbsent(option, (k -> new ArrayList<>()));
    }

    public void removeOption(String option) {
        if (options.containsKey(option)) {
            options.remove(option);
        }
    }

    public void addVoter(String option, String voter) {
        if (!alreadyVoted(voter)) {
            options.get(option).add(voter);
        }
    }

    public void removeVoter(String voter) {
        for (List<String> voters : options.values()) {
            if (voters.contains(voter)) {
                voters.remove(voter);
            }
        }
    }

    public Map<String, Integer> getVoteTally() {
        Map<String, Integer> voteMap = new TreeMap<>();

        options.forEach((option, voters) -> voteMap.put(option, voters.size()));

        return voteMap;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getOwner() {
        return owner;
    }

    public SoapEventType getType() {
        return TYPE;
    }

    public String getChannel() {
        return channel;
    }

    public void setExperation(String timeString) throws IllegalArgumentException {
        try {
            this.expiration = SoapUtility.calculateFutureDateTime(timeString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public int until() {
        LocalTime now = LocalTime.now(ZoneId.of("-05:00")).plusHours(1);
        LocalTime eventTime = LocalTime.parse(expiration);
        long until = now.until(eventTime, ChronoUnit.SECONDS);
        if (until < 0 && Math.abs(until) > 60) {
            until = Math.abs(until);
        }
        return (int) until;
    }

    public boolean same(SoapEvent compare) {
        if (!(compare instanceof PollEvent)) {
            return false;
        }
        
        return identifier.equals(compare.getIdentifier())
            && channel.equals(compare.getChannel());
    }
}
