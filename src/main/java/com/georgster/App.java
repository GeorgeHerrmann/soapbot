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

        /* Creates a manager that will control the SoapClient for each Guild */
        SoapClientManager soapManager = new SoapClientManager(token);
        soapManager.listenToEvents(); //Subscribes the manager to Discord's event stream
        soapManager.start(); //Starts the manager, finalizing the connection to Discord's API
    }

}
