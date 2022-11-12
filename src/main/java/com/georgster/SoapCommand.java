package com.georgster;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class SoapCommand implements Command {
    public void execute(MessageCreateEvent event) {
        String version = "";
        File myObj = new File("pom.xml");
        Scanner myReader;
        try {
            myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                version = myReader.nextLine();
                if (version.contains("<version>")) {
                    version = version.substring(version.indexOf("<version>") + 9, version.indexOf("<version>") + 13);
                    break;
                }
            }
            myReader.close();
            event.getMessage().getChannel().block().createMessage("Soap Bot Version: " + version +
            "\nView my repository and source code at: https://github.com/GeorgeHerrmann/soapbot").block();
        } catch (FileNotFoundException e) {
            event.getMessage().getChannel().block().createMessage("Couldn't find version file").block();
            e.printStackTrace();
        }
    }

    public String help() {
        return "Command: !soapbot " +
        "\nGives information about SOAP Bot";
    }
}
