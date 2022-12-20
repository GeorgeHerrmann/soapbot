package com.georgster.util;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;

/**
 * Intended to house all the general SOAP Bot methods that are not specific to any one class.
 */
public class SoapGeneralHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private SoapGeneralHandler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Sends a message to the channel that the command was sent from.
     * 
     * @param message the message to send
     * @param channel the channel to send the message to
     */
    public static void sendTextMessageInChannel(String message, MessageChannel channel) {
        channel.createMessage(message).block();
    }

    /**
     * Returns the channel that matches the channelName parameter from the list of channels.
     * 
     * @param channelName the name of the channel to match
     * @param channels the list of channels to search through
     * @return the channel that matches the channelName parameter
     */
    public static Channel channelMatcher(String channelName, List<GuildChannel> channels) {
        for (GuildChannel channel : channels) {
            if (channel.getType() == Channel.Type.GUILD_TEXT && channel.getName().equals(channelName))
                return channel;
        }
        return null;
    }

    /**
     * Converts a time string to a standard 24 hour time string
     * for Time classes to use.
     * 
     * @param time the time string to convert
     * @return the converted time string
     */
    public static String timeConverter(String time) throws IllegalArgumentException {
        time = time.toUpperCase();
        int hour;
        int minute;
        try {
            String[] timeArray = time.split(":");
            hour = Integer.parseInt(timeArray[0]);
            minute = Integer.parseInt(timeArray[1].substring(0, 2));
            if (timeArray[1].contains("PM") && hour != 12) {
                hour += 12;
            } else if (timeArray[1].contains("AM") && hour == 12) {
                hour = 0;
            }
        } catch (PatternSyntaxException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            try {
                if (time.contains("PM") && Integer.parseInt(time.substring(0, time.indexOf("PM"))) != 12) {
                    hour = Integer.parseInt(time.substring(0, time.indexOf("PM"))) + 12;
                    minute = 0;
                } else if (time.contains("AM") && Integer.parseInt(time.substring(0, time.indexOf("AM"))) == 12) {
                    hour = 0;
                    minute = 0;
                } else {
                    hour = Integer.parseInt(time.substring(0, time.indexOf("PM")));
                    minute = 0;
                }
            } catch (Exception e2) {
                throw new IllegalArgumentException("Invalid time format, valid times formats are 1:00pm, 1pm, 01:00, 1:00PM, 1PM, 01:00PM, 1:00am, 1am, 01:00am, 1:00AM, 1AM, 01:00AM");
            }
        }
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Creates and immediately starts a new daemon thread that executes
     * {@code target.run()}. This method, which may be called from any thread,
     * will return immediately its the caller.
     * @param target the object whose {@code run} method is invoked when this
     *               thread is started
     */
    public static void runDaemon(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    }

}
