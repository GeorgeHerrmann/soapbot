package com.georgster.util;

import java.util.List;

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
