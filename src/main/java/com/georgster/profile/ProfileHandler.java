package com.georgster.profile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.reserve.ReserveEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.FileWriter;

/**
 * Intended to handle Server and User "profiles" inside of SOAP Bot.
 * A "Server Profile" exists inside a folder dedicated to that {@code Guild} based on its {@code Snowflake} ID.
 * A "User Profile" exists solely inside a JSON file inside of a "Server Profile" directory.
 */
public class ProfileHandler {

    /* Holds the location of the profiles directory */
    private static final String PROFILELOCATION = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "georgster", "profile", "profiles").toString();
    private final String id;

    /**
     * Creates a new {@code ProfileHandler} for a Guild with the
     * associated {@code Snowflake} ID.
     * 
     * @param id the {@code Snowflake} ID of the {@code Guild} that this {@code ProfileHandler} is for.
     */
    public ProfileHandler(String id) {
        this.id = id;
    }

    /**
     * Returns whether or not a user profile exists with the supplimented server and user IDs.
     * 
     * @param userId the {@code Snowflake} id of the {@code Member} who's profile is being examined.
     * @return true if the profile exists already, false otherwise.
     */
    public boolean userProfileExists(String userId) {
        return Files.exists(Paths.get(PROFILELOCATION, id, "users", userId + ".json"));
    }

    /**
     * Creates a user's profile based on the {@code Snowflake} ID of the {@code Guild} profile it should exist in.
     * 
     * @param userId the {@code Snowflake} id of the {@code Member} who's profile is being examined.
     */
    public void createUserProfile(String userId) {
        File profile = new File(Paths.get(PROFILELOCATION, id, "users", userId + ".json").toString());
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
    public void updateUserProfile(UserProfile profile) {
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
     */
    public void createServerProfile() {
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
     * @return true if the profile exists already, false otherwise.
     */
    public boolean serverProfileExists() {
        return (Files.exists(Paths.get(PROFILELOCATION, id), LinkOption.NOFOLLOW_LINKS)) //Checks if the server profile directory exists
        && (Files.exists(Paths.get(PROFILELOCATION, id, "events.json"), LinkOption.NOFOLLOW_LINKS)) //Checks if the events.json file exists
        && (Files.exists(Paths.get(PROFILELOCATION, id, "users"), LinkOption.NOFOLLOW_LINKS)); //Checks if the users directory exists
    }

    /**
     * Adds an object to the server's profile file based on the supplied {@code ProfileType}.
     * 
     * @param object the object to be added to the profile file.
     * @param type the type of the server's profile that the object should be added to.
     */
    public void addObject(Object object, ProfileType type) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        if (serverProfileExists()) { //Writes a JSON representation of the object to the profile file
            try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, id, (type.name().toLowerCase() + ".json")).toString(), true)) {
                writer.write(parser.toJson(object));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Makes a ReserveEvent object from the server's event list.
     * 
     * @param eventIdentifier the identifier of the event to be pulled from the server's event list.
     * @return the {@code ReserveEvent} object that was pulled from the server's event list.
     */
    public ReserveEvent pullEvent(String eventIdentifier) {
        if (serverProfileExists()) {
            try (JsonReader reader = new JsonReader(new FileReader(Paths.get(PROFILELOCATION, id, "events.json").toString()))){
                reader.setLenient(true);
                while (reader.hasNext()) { //Iterates through the events.json file
                    reader.beginObject();
                    int numPeople = 0;
                    int numReserved = 0;
                    String channel = "";
                    List<String> reservedUsers = new ArrayList<>();
                    String time = "";
                    String name = reader.nextName();
                    String identifier = reader.nextString();
                    if (name.equals("identifier") && identifier.equals(eventIdentifier)) { //If the identifier matches the one we're looking for, return the event
                        if (reader.nextName().equals("numPeople")) {
                            numPeople = reader.nextInt();
                        }
                        if (reader.nextName().equals("numReserved")) {
                            numReserved = reader.nextInt();
                        }
                        if (reader.nextName().equals("time")) {
                            time = reader.nextString();
                        }
                        if (reader.nextName().equals("channel")) {
                            channel = reader.nextString();
                        }
                        if (reader.nextName().equals("reservedUsers")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reservedUsers.add(reader.nextString());
                            }
                            reader.endArray();
                        }
                        if (reader.nextName().equals("type")) {
                            reader.skipValue();
                        }
                        return new ReserveEvent(identifier, numPeople, numReserved, time, channel, reservedUsers);
                    } else {
                        while(reader.peek() != JsonToken.END_OBJECT) {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Removes an object from the server's profile file based on the supplied {@code ProfileType}.
     * 
     * @param object the object to be removed from the profile file.
     * @param type the type of the server's profile that the object should be removed from.
     */
    public void removeObject(Object object, ProfileType type) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        if (serverProfileExists()) {
            try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, id, (type.name().toLowerCase() + ".json")).toString(), true)) {
                String objects = Files.readString(Paths.get(PROFILELOCATION, id, (type.name().toLowerCase() + ".json")));
                wipe(type);
                objects = objects.replace(parser.toJson(object), ""); //Remove the event from the file
                writer.write(objects);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Wipes the server's profile file based on the supplied {@code ProfileType}.
     * 
     * @param type the type of the server's profile that should be wiped.
     */
    public void wipe(ProfileType type) {
        if (serverProfileExists()) {
            try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, id, (type.name().toLowerCase() + ".json")).toString())) {
                writer.write("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if an event exists in the server's event list.
     * 
     * @param identifier the identifier of the event to be checked against the server's event list.
     * @return true if the event exists in the server's event list, false otherwise.
     */
    public boolean eventExists(String identifier) {
        if (serverProfileExists()) {
            try  {
                String contents = Files.readString(Path.of(PROFILELOCATION, id, "events.json"));
                return contents.contains(": \"" + identifier + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Checks if an object exists in the server's profile file based on the supplied {@code ProfileType}.
     * 
     * @param object the object to be checked against the profile file.
     * @param type the type of the server's profile that the object should be checked against.
     * @return true if the object exists in the server's profile file, false otherwise.
     */
    public boolean objectExists(Object object, ProfileType type) {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();
        if (serverProfileExists()) {
            try {
                String contents = Files.readString(Path.of(PROFILELOCATION, id, (type.name().toLowerCase() + ".json")));
                return contents.contains(parser.toJson(object));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Gets the list of events from the server's event list.
     * 
     * @param id the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @return the list of {@code ReserveEvent} objects from the server's event list.
     */
    public List<SoapEvent> getEvents() {
        List<SoapEvent> events = new ArrayList<>();
        if (serverProfileExists()) {
            try (JsonReader reader = new JsonReader(new FileReader(Paths.get(PROFILELOCATION, id, "events.json").toString()))){
                reader.setLenient(true);
                while (reader.hasNext()) {
                    reader.beginObject();
                    List<String> reservedUsers = new ArrayList<>();
                    int numPeople = 0;
                    int numReserved = 0;
                    String channel = "";
                    String time = "";
                    String name = reader.nextName();
                    String identifier = reader.nextString();
                    if (name.equals("identifier")) {
                        if (reader.nextName().equals("numPeople")) {
                            numPeople = reader.nextInt();
                        }
                        if (reader.nextName().equals("numReserved")) {
                            numReserved = reader.nextInt();
                        }
                        if (reader.nextName().equals("time")) {
                            time = reader.nextString();
                        }
                        if (reader.nextName().equals("channel")) {
                            channel = reader.nextString();
                        }
                        if (reader.nextName().equals("reservedUsers")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reservedUsers.add(reader.nextString());
                            }
                            reader.endArray();
                        }
                        if (reader.nextName().equals("type")) {
                            reader.skipValue();
                        }
                        events.add(new ReserveEvent(identifier, numPeople, numReserved, time, channel, reservedUsers)); //Add each event to the list
                    } else {
                       while(reader.peek() != JsonToken.END_OBJECT) {
                           reader.skipValue();
                       }
                    }
                    reader.endObject();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return events;
    }

    public List<SoapEvent> getEvents(SoapEventType type) {
        List<SoapEvent> events = new ArrayList<>();
        if (serverProfileExists()) {
            try (JsonReader reader = new JsonReader(new FileReader(Paths.get(PROFILELOCATION, id, "events.json").toString()))){
                reader.setLenient(true);
                while (reader.hasNext()) {
                    reader.beginObject();
                    List<String> reservedUsers = new ArrayList<>();
                    int numPeople = 0;
                    int numReserved = 0;
                    String channel = "";
                    String time = "";
                    String name = reader.nextName();
                    String identifier = reader.nextString();
                    if (name.equals("identifier")) {
                        if (reader.nextName().equals("numPeople")) {
                            numPeople = reader.nextInt();
                        }
                        if (reader.nextName().equals("numReserved")) {
                            numReserved = reader.nextInt();
                        }
                        if (reader.nextName().equals("time")) {
                            time = reader.nextString();
                        }
                        if (reader.nextName().equals("channel")) {
                            channel = reader.nextString();
                        }
                        if (reader.nextName().equals("reservedUsers")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reservedUsers.add(reader.nextString());
                            }
                            reader.endArray();
                        }
                        if (reader.nextName().equals("type")) {
                            if (reader.nextString().equals(type.name())) {
                                events.add(new ReserveEvent(identifier, numPeople, numReserved, time, channel, reservedUsers)); //Add each event to the list
                            }
                        }
                    } else {
                       while(reader.peek() != JsonToken.END_OBJECT) {
                           reader.skipValue();
                       }
                    }
                    reader.endObject();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return events;
    }

    /**
     * Checks if the server has any events.
     * 
     * @param id the {@code Snowflake} id of the {@code Guild} where the profile folder would be located.
     * @return {@code true} if the server has events, {@code false} otherwise.
     */
    public boolean areEvents() {
        if (serverProfileExists()) {
            try {
                String contents = Files.readString(Path.of(PROFILELOCATION, id, "events.json"));
                return contents.contains("identifier");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
