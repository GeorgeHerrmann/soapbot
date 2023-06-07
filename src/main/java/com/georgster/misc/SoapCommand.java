package com.georgster.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A SoapCommand is represents the actions following a "!soapbot" command.
 */
public class SoapCommand implements Command {

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();

        String version = "";
        File myObj = new File("pom.xml"); //Reads the version number from the pom.xml file
        Scanner myReader;
        try {
            myReader = new Scanner(myObj);
            logger.append("\tReading SOAPBot's version information from its XML file", LogDestination.NONAPI);
            while (myReader.hasNextLine()) {
                version = myReader.nextLine();
                if (version.contains("<version>")) {
                    version = version.substring(version.indexOf("<version>") + 9, version.indexOf("<version>") + 14);
                    break;
                }
            }
            myReader.close();
            handler.sendText("Version: " + version +
            "\nView my repository and source code at: https://github.com/GeorgeHerrmann/soapbot", "SOAP Bot");
        } catch (FileNotFoundException e) { //Should only be thrown if there is an issue with the pom.xml file
            logger.append("- Couldn't find the version file", LogDestination.NONAPI);
            handler.sendText("Couldn't find version file");
            e.printStackTrace();
        }
        logger.append("- Showing information about SOAP Bot in a text channel", LogDestination.API);

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
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Show information about SOAP Bot")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\nGives information about SOAP Bot";
    }

}
