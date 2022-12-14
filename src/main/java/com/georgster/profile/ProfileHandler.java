package com.georgster.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import com.georgster.reserve.ReserveEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.FileWriter;

/**
 * Intended to handle Server and User "profiles" inside of SOAP Bot.
 * A "Server Profile" exists inside a folder dedicated to that {@code Guild} based on its {@code Snowflake} ID.
 * A "User Profile" exists solely inside a JSON file inside of a "Server Profile" directory.
 */
public class ProfileHandler {

    /* Holds the location of the profiles directory */
    private static final String PROFILELOCATION = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "georgster", "profile", "profiles").toString();

    /**
     * ProfileHandler is a utility class and does not need to be constructed.
     */
    private ProfileHandler() {
        throw new IllegalStateException("ProfileHandler is not meant to be constructed");
    }

    /**
     * Returns whether or not a user profile exists with the supplimented server and user IDs.
     * 
     * @param userId the {@code Snowflake} id of the {@code Member} who's profile is being examined.
     * @param serverId the {@code Snowflake} id of the {@code Guild} where the user's profile for that Guild would be located.
     * @return true if the profile exists already, false otherwise.
     */
    public static boolean userProfileExists(String userId, String serverId) {
        return Files.exists(Paths.get(PROFILELOCATION, serverId, "users", userId + ".json"));
    }

    /**
     * Creates a user's profile based on the {@code Snowflake} ID of the {@code Guild} profile it should exist in.
     * 
     * @param userId the {@code Snowflake} id of the {@code Member} who's profile is being examined.
     * @param serverId the {@code Snowflake} id of the {@code Guild} where the user's profile for that Guild would be located.
     */
    public static void createUserProfile(String userId, String serverId) {
        File profile = new File(Paths.get(PROFILELOCATION, serverId, "users", userId + ".json").toString());
        try {
            if (!profile.createNewFile()) {
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a user's profile with any new data help inside their {@code Profile} object.
     * Uses Gson to translate the {@code Profile} object to JSON format to be saved.
     * 
     * @param profile The object containing all data associated with this {@code Member}.
     */
    public static void updateUserProfile(UserProfile profile) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, profile.getGuildId(), "users", profile.getMemberId() + ".json").toString())) {
            writer.write(parser.toJson(profile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a servers's profile based on the {@code Snowflake} ID of the {@code Guild}.
     * A profile for a {@code Guild} exists inside a dedicated directory for that {@code Guild}.
     * 
     * @param serverId the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     */
    public static void createServerProfile(String id) {
        File profile = new File(Paths.get(PROFILELOCATION, id).toString());
        profile.mkdir();
        profile = new File(Paths.get(PROFILELOCATION, id, "events.json").toString());
        try {
            if (!profile.createNewFile()) {
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        profile = new File(Paths.get(PROFILELOCATION, id, "users").toString());
        profile.mkdir();
    }

    /**
     * Returns whether or not a server profile exists with the supplimented server ID.
     * 
     * @param serverId the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @return true if the profile exists already, false otherwise.
     */
    public static boolean serverProfileExists(String id) {
        return (Files.exists(Paths.get(PROFILELOCATION, id), LinkOption.NOFOLLOW_LINKS)) //Checks if the server profile directory exists
        && (Files.exists(Paths.get(PROFILELOCATION, id, "events.json"), LinkOption.NOFOLLOW_LINKS)) //Checks if the events.json file exists
        && (Files.exists(Paths.get(PROFILELOCATION, id, "users"), LinkOption.NOFOLLOW_LINKS)); //Checks if the users directory exists
    }

    /**
     * Adds an event to the server's event list.
     * 
     * @param id the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @param event the {@code ReserveEvent} object to be added to the server's event list.
     */
    public static void addEvent(String id, ReserveEvent event) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        if (serverProfileExists(id)) {
            try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, id, "events.json").toString(), true)) {
                writer.write(parser.toJson(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if an event is already in the server's event list.
     * 
     * @param id the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @param event the {@code ReserveEvent} object to be checked against the server's event list.
     * @return true if the event is already in the server's event list, false otherwise.
     */
    public static boolean checkEventDuplicates(String id, ReserveEvent event) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        String eventJson = "";
        if (serverProfileExists(id)) {
            JsonElement element = parser.toJsonTree(event);
            if (element.isJsonObject()) {
                eventJson = element.getAsJsonObject().get("identifier").getAsString();
                try {
                    String events = new String(Files.readAllBytes(Paths.get(PROFILELOCATION, id, "events.json")));
                    if (events.contains(": \"" + eventJson + "\"")) { //If the provided ReserveEvent is in the file, return true
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false; //If any
    }

    /**
     * Makes a ReserveEvent object from the server's event list.
     * 
     * @param id the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @param eventIdentifier the identifier of the event to be pulled from the server's event list.
     * @return the {@code ReserveEvent} object that was pulled from the server's event list.
     */
    public static ReserveEvent pullEvent(String id, String eventIdentifier) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        if (serverProfileExists(id)) {
            try {
                String events = new String(Files.readAllBytes(Paths.get(PROFILELOCATION, id, "events.json")));
                if (events.contains(": \"" + eventIdentifier + "\"")) { //If the provided ReserveEvent is in the file
                    events = events.substring(events.indexOf();
                    return parser.fromJson(events, ReserveEvent.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Removes an event from the server's event list.
     * 
     * @param id the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @param event the {@code ReserveEvent} object to be removed from the server's event list.
     */
    public static void removeEvent(String id, ReserveEvent event) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        if (serverProfileExists(id)) {
            try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, id, "events.json").toString())) {
                String events = new String(Files.readAllBytes(Paths.get(PROFILELOCATION, id, "events.json")));
                if (events.contains(": \"" + event.getIdentifier() + "\"")) { //If the provided ReserveEvent is in the file
                    events = events.replace(parser.toJson(event), ""); //Remove the event from the file
                    writer.write(events);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
