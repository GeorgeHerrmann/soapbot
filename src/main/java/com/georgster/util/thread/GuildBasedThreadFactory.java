package com.georgster.util.thread;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that provides basic threads with naming schemes
 * based on a Guild's name and a general descriptor. Intended to be used with an ExecutorService.
 */
public class GuildBasedThreadFactory implements ThreadFactory {
    private final String descriptor;
    private final String guildName;

    /**
     * Creates a GuildBasedThreadFactory which will name Threads based on the
     * guildName and descriptor.
     * 
     * @param guildName The name of the guild.
     * @param descriptor A general descriptor for created threads.
     */
    protected GuildBasedThreadFactory(String guildName, String descriptor) {
        this.descriptor = descriptor.toUpperCase();
        this.guildName = guildName;
    }

    /**
     * {@inheritDoc}
     */
    public Thread newThread(Runnable runnable) {
        String name = guildName + "-" + descriptor;
        return new Thread(runnable, name);
    }
}
