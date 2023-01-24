package com.georgster.api;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * The ActionWriter sends data to and from the SOAP API.
 */
public class ActionWriter {

    private static final String WRITERLOCATION = Paths.get(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "api", "data", "currentaction.json").toString();

    /**
     * Prevents instantiation of this utility class.
     */
    private ActionWriter() {
        throw new IllegalStateException("ActionWriter is a utility class");
    }

    /**
     * Writes an action from SOAP Bot's systems to the SOAP API.
     * 
     * @param action The action to be written.
     */
    public static void writeAction(String action) {
        try (FileWriter writer = new FileWriter(WRITERLOCATION)) {
            writer.write(action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
