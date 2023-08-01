package com.georgster.util.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import discord4j.core.object.entity.Guild;

/**
 * A factory class for creating and managing thread pools for each {@code SoapClient}.
 * <p>
 * <p>Maximum concurrent tasks for each Guild's thread pools:</p>
 * <ul>
 *  <li>General: unlimited</li>
 *  <li>Event: 30</li>
 *  <li>Command: 30</li>
 *  <li>Voice: 1</li>
 *  <li>Global Discord API calls: 1</li>
 */
public class ThreadPoolFactory {
    private static final Map<String, ThreadPoolManager> CLIENT_THREAD_POOL_MANAGERS = new ConcurrentHashMap<>(); // Maps guild IDs to thread pool managers

    /**
     * Private constructor to prevent instantiation.
     */
    private ThreadPoolFactory() {
        throw new IllegalStateException("Utility factory class");
    }

    /**
     * Creates a new thread pool manager for the given guild ID if one does not already exist.
     * 
     * @param guildId The guild ID to create a thread pool manager for
     */
    public static void createThreadPoolManager(Guild guild) {
        CLIENT_THREAD_POOL_MANAGERS.computeIfAbsent(guild.getId().asString(), id -> new ThreadPoolManager(id, guild.getName()));
    }

    /**
     * Schedules a task to be executed by the general pool manager for the given guild ID.
     * 
     * @param guildId The guild ID to schedule the task for
     * @param task   The task to be executed
     */
    public static void scheduleGeneralTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleGeneralTask(task);
    }

    /**
     * Schedules a task to be executed by the event thread pool manager for the given guild ID.
     * Each Guild can schedule up to 30 events at a time.
     * 
     * @param guildId The guild ID to schedule the task for
     * @param task  The task to be executed
     */
    public static void scheduleEventTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleEventTask(task);
    }

    /**
     * Schedules a task to be executed by the command thread pool manager for the given guild ID.
     * Each Guild can schedule up to 30 commands at a time.
     * 
     * @param guildId The guild ID to schedule the task for
     * @param task The task to be executed
     */
    public static void scheduleCommandTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleCommandTask(task);
    }

    /**
     * Schedules a task to be executed by the voice thread pool manager for the given guild ID.
     * Each Guild can schedule only one voice task at a time.
     * 
     * @param guildId The guild ID to schedule the task for
     * @param task The task to be executed
     */
    public static void scheduleVoiceTask(String guildId, Runnable task) {
        CLIENT_THREAD_POOL_MANAGERS.get(guildId).scheduleVoiceTask(task);
    }

    /**
     * Schedules a task to be executed by the global thread pool manager.
     * <b>NOT</b> to be used for Guild specific tasks.
     * All of SOAPBot can schedule only one global discord api task at a time.
     * 
     * @param task The task to be executed
     */
    public static void scheduleGlobalDiscordApiTask(Runnable task) {
        ThreadPoolManager.scheduleGlobalDiscordApiCallTask(task);
    }
}
