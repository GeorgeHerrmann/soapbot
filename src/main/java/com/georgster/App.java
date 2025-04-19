package com.georgster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.georgster.control.SoapClientManager;

/**
 * The main class for SoapBot.
 */
public class App {

    public static void main(String[] args) {
      
        String token = "";
        try { // Gets the API key
          token = Files.readString( Path.of(System.getProperty("user.dir"), "key.txt") );
        } catch (IOException e) { // If the key file doesn't exist, the program will exit
          e.printStackTrace();
          System.exit(1);
        }

        /* Creates a manager that will control the SoapClient for each Guild */
        SoapClientManager soapManager = new SoapClientManager(token);
        soapManager.listenToEvents(); //Subscribes the manager to Discord's event stream
        soapManager.enableTestMode();
        soapManager.start(); //Starts the manager, finalizing the connection to Discord's API
    }
}
