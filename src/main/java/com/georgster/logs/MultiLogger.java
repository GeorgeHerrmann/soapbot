package com.georgster.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.georgster.api.ActionWriter;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.SoapUtility;

/**
 * Logs information to {@code LogDestinations} about SOAP Bot's systems.
 */
public class MultiLogger {
    private static final String LOGFILELOCATION = Paths.get(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "logs", "data").toString();

    private final EnumMap<LogDestination, String> logs; //Each log message is mapped to its destination
    private final GuildInteractionHandler handler;
    private final Class<?> source;

    /**
     * Creates a MultiLogger for the associated {@code Guild} in the {@code GuildInteractionHandler}
     * from the source of the specified class.
     * 
     * @param manager The handler of the {@code Guild} for logs to be sent to.
     * @param source The class of the object that is logging.
     */
    public MultiLogger(GuildInteractionHandler handler, Class<?> source) {
        logs = new EnumMap<>(LogDestination.class);
        this.handler = handler;
        this.source = source;
    }

    /**
     * Appends a message to the log that is bound for its {@code LogDestination}.
     * 
     * @param text The message to be appended.
     * @param destinations The {@code LogDestination} where the message will be sent.
     */
    public void append(String text, LogDestination ...destinations) {
        for (LogDestination destination : destinations) {
            logs.put(destination, logs.getOrDefault(destination, "") + text);
        }
    }

    /**
     * Removes log messages from a destination, if any exist, from the log.
     * 
     * @param destination The destination to wipe.
     */
    public void remove(LogDestination destination) {
        logs.remove(destination);
    }

    /**
     * Clears all log messages from the log.
     */
    public void clear() {
        logs.clear();
    }

    /**
     * Sends the log messages bound for the specified {@code LogDestination} to their
     * associated {@code LogDestinations}, then clears the log for that destination.
     * 
     * @param destination The destination to send.
     */
    public void send(LogDestination destination) {
        switch (destination) {
            case DISCORD:
                logDiscord(logs.getOrDefault(LogDestination.DISCORD, ""));
                break;
            case SYSTEM:
                logSystem(logs.getOrDefault(LogDestination.SYSTEM, ""));
                break;
            case FILE:
                logFile(logs.getOrDefault(LogDestination.FILE, ""));
                break;
            case API:
                logApi(logs.getOrDefault(LogDestination.API, ""));
                break;
            case NONAPI:
                logNonApi(logs.getOrDefault(LogDestination.NONAPI, ""));
        }
        remove(destination);
    }

    /**
     * Sends all log messages to their associated {@code LogDestinations},
     * then clears the log.
     */
    public void sendAll() {
        logDiscord(logs.getOrDefault(LogDestination.DISCORD, ""));
        logSystem(logs.getOrDefault(LogDestination.SYSTEM, ""));
        logFile(logs.getOrDefault(LogDestination.FILE, ""));
        logApi(logs.getOrDefault(LogDestination.API, ""));
        logNonApi(logs.getOrDefault(LogDestination.NONAPI, ""));
        clear();
    }

    /**
     * Logs a message to the DISCORD, SYSTEM and FILE {@code LogDestinations}.
     * 
     * @param nonApi The message to be logged.
     */
    public void logNonApi(String nonApi) {
        if (!nonApi.isEmpty()) {
            logDiscord(nonApi);
            logSystem(nonApi);
            logFile(nonApi);
        }
    }

    /**
     * Logs a message to the {@code Guild's} bot-logs channel.
     * 
     * @param discord The message to be logged.
     */
    public void logDiscord(String discord) {
        if (canLog() && !discord.isEmpty()) {
            String[] output = SoapUtility.splitFirst(discord);
            GuildInteractionHandler.sendText(output[1], output[0], handler.getTextChannel("bot-logs"));
        }
    }

    /**
     * Logs a message to the system's console.
     * 
     * @param system The message to be logged.
     */
    public void logSystem(String system) {
        if (!system.isEmpty()) {
            system = system.replace("- ", "\t");
            Logger logger = LoggerFactory.getLogger(source);
            logger.info(system);
        }
    }

    /**
     * Logs a message to SOAP Bot's log file.
     * 
     * @param file The message to be logged.
     */
    public void logFile(String file) {
        if (!file.isEmpty()) {
            try (FileWriter writer = new FileWriter(Paths.get(LOGFILELOCATION, "log.txt").toString(), true)) {
                writer.write(source.getName() + ": " + file.replace("- ", "\t") + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Logs a message to SOAP Bot's {@code ActionWriter} which
     * communicates with the SOAP Api.
     * 
     * @param api The message to be logged.
     */
    public void logApi(String api) {
        if (!api.isEmpty()) {
            api = api.replace("\t", "").replace("\n", "").replace("- ", "");
            ActionWriter.writeAction(api);
        }
    }

    /**
     * Returns whether or not this logger can log to the {@code Guild's} bot-logs channel.
     * 
     * @return {@code true} if the bot-logs channel can be logged to, {@code false} otherwise.
     */
    private boolean canLog() {
        return (handler.getTextChannel("bot-logs") != null);
    }

    /**
     * Wipes the log file.
     */
    public static void wipeFileLogs() {
        verifyLogFile();
        try {
            Files.delete(Path.of(LOGFILELOCATION, "log.txt"));
            Files.createFile(Path.of(LOGFILELOCATION, "log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifies that the log file exists, and creates it if it does not.
     */
    private static void verifyLogFile() {
        if (!Files.exists(Path.of(LOGFILELOCATION, "log.txt"))) {
            try {
                new File(LOGFILELOCATION).mkdir();
                Files.createFile(Path.of(LOGFILELOCATION, "log.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
