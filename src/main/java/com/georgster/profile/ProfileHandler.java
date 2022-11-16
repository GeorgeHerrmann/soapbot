package com.georgster.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import com.google.gson.Gson;
import java.io.FileWriter;

/*
 * Intended to handle Server and User "profiles" inside of SOAP Bot.
 */
public class ProfileHandler {

    static final String PROFILELOCATION = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "georgster", "profile", "profiles").toString();

    private ProfileHandler() {
        throw new IllegalStateException("ProfileHandler is not meant to be constructed");
    }

    public static boolean userProfileExists(String userId, String serverId) {
        return Files.exists(Paths.get(PROFILELOCATION, serverId, "users", userId + ".json"));
    }

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

    public static void updateUserProfile(Profile profile) {
        Gson parser = new Gson();
        try (FileWriter writer = new FileWriter(Paths.get(PROFILELOCATION, profile.getGuildId(), "users", profile.getMemberId() + ".json").toString())) {
            writer.write(parser.toJson(profile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createServerProfile(String id) {
        File profile = new File(Paths.get(PROFILELOCATION, id).toString());
        profile.mkdir();
        profile = new File(Paths.get(PROFILELOCATION, id, "users").toString());
        profile.mkdir();
    }

    public static boolean serverProfileExists(String id) {
        return Files.exists(Paths.get(PROFILELOCATION, id), LinkOption.NOFOLLOW_LINKS);
    }
}
