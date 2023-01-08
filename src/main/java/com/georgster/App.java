package com.georgster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The main class for SoapBot.
 */
public class App {

    public static void main(String[] args) {
        
        String token = "";
        try {
          token = Files.readString( Path.of(System.getProperty("user.dir"), "key.txt") );
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(0);
        }
        SoapClient client = new SoapClient(token);
        client.listenToEvents();
        client.start();
    }

}
