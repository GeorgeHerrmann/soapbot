package com.georgster.profile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

/*
 * Intended to handle Server and User "profiles" inside of SOAP Bot.
 */
public class ProfileHandler {
    /*
     * Profile Structure:
     * Folder for each server "id"
     * Inside each folder is folder for users, with a file for each user
     * The server folder can also hold other information.
     * The structure for data inside is:
     * dataID {
     *  Data;
     * }
     */

    static final String PROFILELOCATION = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "com", "georgster", "profile").toString();

    private ProfileHandler() {
        throw new IllegalStateException("ProfileHandler is not meant to be constructed");
    }

    public static boolean userProfileExists(String id) {
        
    }

    public static void createServerProfile(String id) {
        File profile = new File(Paths.get(PROFILELOCATION, id).toString());
        profile.mkdir();
    }

    public static boolean serverProfileExists(String id) {
        return Files.exists(Paths.get(PROFILELOCATION, id), LinkOption.NOFOLLOW_LINKS);
    }
}
