package com.georgster.util.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Manages thread pools for each {@code SoapClient}.</p>
 * <p>Each task scheduled will be executed immediately if a thread is available, otherwise it will be queued.</p>
 * <p>Maximum concurrent tasks for each thread pool:</p>
 * <ul>
 *  <li>Database: unlimited</li>
 *  <li>Event: 10</li>
 *  <li>Command: 3</li>
 *  <li>Voice: 1</li>
 * </ul>
 */
public class ThreadPoolManager {
    private String guildId; // The guild ID that this thread pool manager is for

    private final ExecutorService databaseThreadPool = Executors.newCachedThreadPool(); 
    private final ExecutorService eventThreadPool = Executors.newFixedThreadPool(10); //Can schedule 10 events at once
    private final ExecutorService commandThreadPool = Executors.newFixedThreadPool(3); // Can schedule 3 commands at once
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
     * Schedules a task to be executed by the database thread pool manager.
     * 
     * @param task The task to be executed
     * @deprecated causing race conditions, use {@link #scheduleCommandTask(Runnable)} instead
     */
    @Deprecated
    protected void scheduleDatabaseTask(Runnable task) {
        databaseThreadPool.submit(task);
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
}
