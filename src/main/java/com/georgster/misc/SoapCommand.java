package com.georgster.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import com.georgster.Command;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
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
        MultiLogger<SoapCommand> logger = new MultiLogger<>(manager, SoapCommand.class);
        logger.append("Executing: " + this.getClass().getSimpleName() + "\n", LogDestination.NONAPI);

        String version = "";
        File myObj = new File("pom.xml"); //Reads the version number from the pom.xml file
        Scanner myReader;
        try {
            myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                logger.append("\tReading SOAPBot's version information from its XML file", LogDestination.NONAPI);
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
            logger.append("\tCouldn't find the version file", LogDestination.NONAPI);
            manager.sendText("Couldn't find version file");
            e.printStackTrace();
        }
        logger.append("Showing information about SOAP Bot in a text channel", LogDestination.API);

        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("soapbot", "version", "info", "about", "bot");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !soapbot " +
        "\nAliases: " + getAliases().toString() +
        "\nGives information about SOAP Bot";
    }

}
