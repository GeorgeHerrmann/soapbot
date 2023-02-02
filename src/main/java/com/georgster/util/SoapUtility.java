package com.georgster.util;

import java.util.regex.PatternSyntaxException;

/**
 * Intended to house all the general SOAP Bot methods that are not specific to any one class.
 */
public class SoapUtility {

    /**
     * Private constructor to prevent instantiation.
     */
    private SoapUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a time string to a standard 24 hour time string
     * for Time classes to use.
     * 
     * @param time the time string to convert
     * @return the converted time string
     */
    public static String timeConverter(String time) throws IllegalArgumentException {
        time = time.toUpperCase(); //Users can use either 'am' or 'AM'
        int hour;
        int minute;
        try { //If the time string is in the format 1:00pm, 1:00PM, 01:00, 01:00PM, etc.
            String[] timeArray = time.split(":");
            hour = Integer.parseInt(timeArray[0]);
            minute = Integer.parseInt(timeArray[1].substring(0, 2));
            if (timeArray[1].contains("PM") && hour != 12) {
                hour += 12;
            } else if (timeArray[1].contains("AM") && hour == 12) {
                hour = 0;
            }
        } catch (PatternSyntaxException | NumberFormatException | ArrayIndexOutOfBoundsException e) { //If the time string is in the format 1pm, 1PM, etc.
            try {
                if (time.contains("PM")) {
                    hour = Integer.parseInt(time.substring(0, time.indexOf("PM")));
                    hour = Integer.parseInt(time.substring(0, time.indexOf("PM"))) == 12 ? hour : hour + 12;
                    minute = 0;
                } else if (time.contains("AM")) {
                    if (Integer.parseInt(time.substring(0, time.indexOf("AM"))) == 12) {
                        hour = 0;
                        minute = 0;
                    } else {
                        hour = Integer.parseInt(time.substring(0, time.indexOf("AM")));
                        minute = 0;
                    }
                } else {
                    throw new IllegalArgumentException("Does not specify AM/PM time");
                }
            } catch (Exception e2) { //If the time string is in an invalid format, it will get caught here
                throw new IllegalArgumentException("Invalid time format, valid times formats are 1:00pm, 1pm, 01:00, 1:00PM, 1PM, 01:00PM, 1:00am, 1am, 01:00am, 1:00AM, 1AM, 01:00AM");
            }
        }
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Converts a 24 hour time string to a 12 hour time string.
     * Note that this method does not check for valid time strings,
     * it is intended to be used after the timeConverter method.
     * 
     * @param time the time string to convert
     * @return the converted time string
     */
    public static String convertToAmPm(String time) {
        String[] timeArray = time.split(":"); //We can assume an exact format here because the timeConverter method should have already been called
        int hour = Integer.parseInt(timeArray[0]);
        int minute = Integer.parseInt(timeArray[1]);
        String amPm = "AM";
        if (hour > 12) {
            hour -= 12;
            amPm = "PM";
        } else if (hour == 12) {
            amPm = "PM";
        } else if (hour == 0) {
            hour = 12;
        }
        return String.format("%02d:%02d%s", hour, minute, amPm);
    }

    /**
     * Split a string into two parts at the first occurrence of the new line character.
     * 
     * @param line the string to split
     * @return an array of two strings, the first string is the part before the new line character,
     *        the second string is the part after the new line character
     */
    public static String[] splitFirst(String line) {
        String[] split = new String[2];
        split[0] = line.substring(0, line.indexOf("\n"));
        split[1] = line.substring(line.indexOf("\n") + 1);
        return split;
    }

    /**
     * Creates and immediately starts a new daemon thread that executes
     * {@code target.run()}. This method, which may be called from any thread,
     * will return immediately its the caller.
     * @param target the object whose {@code run} method is invoked when this
     *               thread is started
     */
    public static void runDaemon(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true); //Daemon threads will not prevent the program from exiting
        t.start();
    }

}
