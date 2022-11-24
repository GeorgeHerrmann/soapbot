package com.georgster.api;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/*
 * The way this class is currently implemented is a temporary setup which writes to local JSON files
 * for SOAPApi to read from and allow GET requests to, creating a very simple database of sorts.
 * Eventually the goal is to either create a local SQL server or use Mongoose to handle this.
 */
public class ActionWriter {

    private static final String WRITERLOCATION = Paths.get(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "api", "data", "currentaction.json").toString();

    private ActionWriter() {
        throw new IllegalStateException("ActionWriter is a utility class");
    }

    public static void writeAction(String action) {
        try (FileWriter writer = new FileWriter(WRITERLOCATION)) {
            writer.write(action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
