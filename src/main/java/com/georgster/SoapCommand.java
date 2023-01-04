package com.georgster;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.georgster.api.ActionWriter;
import com.georgster.util.GuildManager;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * A SoapCommand is represents the actions following a "!soapbot" command.
 */
public class SoapCommand implements Command {

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        String version = "";
        File myObj = new File("pom.xml"); //Reads the version number from the pom.xml file
        Scanner myReader;
        try {
            myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                ActionWriter.writeAction("Reading SOAPBot's version information from an XML file");
                version = myReader.nextLine();
                if (version.contains("<version>")) {
                    version = version.substring(version.indexOf("<version>") + 9, version.indexOf("<version>") + 14);
                    break;
                }
            }
            myReader.close();
            manager.sendText("Soap Bot Version: " + version +
            "\nView my repository and source code at: https://github.com/GeorgeHerrmann/soapbot");
        } catch (FileNotFoundException e) { //Should only be thrown if there is an issue with the pom.xml file
            manager.sendText("Couldn't find version file");
            e.printStackTrace();
        }
        ActionWriter.writeAction("Showing information about SOAP Bot in a text channel");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !soapbot " +
        "\nGives information about SOAP Bot";
    }

}
