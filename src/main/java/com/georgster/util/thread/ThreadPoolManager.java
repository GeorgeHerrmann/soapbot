package com.georgster.util.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Manages thread pools for each {@code SoapClient}.</p>
 * <p>Each task scheduled will be executed immediately if a thread is available, otherwise it will be queued.</p>
 * <p>Maximum concurrent tasks for each thread pool:</p>
 * <ul>
 *  <li>General: unlimited</li>
 *  <li>Event: 30</li>
 *  <li>Command: 30</li>
 *  <li>Voice: 1</li>
 *  <li>Global Discord API calls: 1</li>
 * </ul>
 */
public class ThreadPoolManager {
    private String guildId; // The guild ID that this thread pool manager is for

    private static final ExecutorService GLOBAL_DISCORD_API_CALL_POOL = Executors.newSingleThreadExecutor(); // Responsible for all global Discord API calls

    private final ExecutorService generalThreadPool = Executors.newCachedThreadPool(); 
    private final ExecutorService eventThreadPool = Executors.newFixedThreadPool(30); //Can schedule 30 events at once
    private final ExecutorService commandThreadPool = Executors.newFixedThreadPool(30); // Can schedule 30 commands at once
    private final ExecutorService voiceThreadPool = Executors.newSingleThreadExecutor(); // Can schedule 1 voice task at once

    /**
     * Creates a new thread pool manager for the given guild ID.
     * 
     * @param guildId The guild ID to create a thread pool manager for
     */
    protected ThreadPoolManager(String guildId) {
        this.guildId = guildId;
    }

    /**
     * Returns the guild ID that this thread pool manager is for.
     * 
     * @return The guild ID that this thread pool manager is for
     */
    protected String getGuildId() {
        return guildId;
    }

    /**
     * Schedules a task to be executed by the general thread pool manager.
     * 
     * @param task The task to be executed
     */
    protected void scheduleGeneralTask(Runnable task) {
        generalThreadPool.submit(task);
    }

    /**
     * Schedule a task to be executed by the event thread pool manager.
     * 
     * @param task The task to be executed
     */
    protected void scheduleEventTask(Runnable task) {
        eventThreadPool.submit(task);
    }

    /**
     * Schedule a task to be executed by the command thread pool manager.
     * 
     * @param task The task to be executed
     */
    protected void scheduleCommandTask(Runnable task) {
        commandThreadPool.submit(task);
    }

    /**
     * Schedule a task to be executed by the voice thread pool manager.
     * 
     * @param task The task to be executed
     */
    protected void scheduleVoiceTask(Runnable task) {
        voiceThreadPool.submit(task);
    }

    /**
     * Schedule a task to be executed by the global Discord API call thread pool manager.
     * 
     * @param task The task to be executed
     */
    protected static void scheduleGlobalDiscordApiCallTask(Runnable task) {
        GLOBAL_DISCORD_API_CALL_POOL.submit(task);
    }
}
