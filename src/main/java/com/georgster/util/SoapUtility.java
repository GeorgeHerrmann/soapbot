package com.georgster.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
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
     * Corrects spacing issues in date strings.
     * 
     * @param input the date string to correct
     * @return the corrected date string
     */
    private static String insertSpaces(String input) {
        // Insert spaces between month/day and day suffix (st/nd/rd/th)
        input = input.replaceAll("(?<=[a-zA-Z])(?=\\d)", " ");
        input = input.replaceAll("(?<=\\d)(?=[a-zA-Z]{2})", " ");
        return input;
    }

    /**
     * Parses a String describing a month into an standardized Date string.
     * 
     * @param inputDate the date string to parse
     * @return the parsed date string
     * @throws IllegalArgumentException if the input date string is invalid
     */
    public static String convertDate(String inputDate) throws IllegalArgumentException {
        inputDate = insertSpaces(inputDate);
        LocalDate date = null;
        try {
            date = parseSpecificDate(inputDate);
        } catch (DateTimeParseException e) {
            try {
                if (inputDate.equalsIgnoreCase("tomorrow")) {
                    date = LocalDate.now().plusDays(1);
                } else {
                    String[] parts = inputDate.split(" ");
                    int daysToAdd = Integer.parseInt(parts[1]);
                    date = LocalDate.now().plusDays(daysToAdd);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date format, valid date formats are MMM dd, yyyy, MMM dd, yy, MMM dd, yyyy, tomorrow, and in x days");
            }
        }
        return date.toString();
    }

    /**
     * Formats a standardized date string into a more readable format.
     * 
     * @param dateTimeString the date string to format
     * @return the formatted date string
     */
    public static String formatDate(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        LocalDate date = LocalDate.parse(dateTimeString);
        return date.format(formatter);
    }

    /**
     * Parses an exact date string (ex: "dec 25th") into a LocalDate object.
     * 
     * @param inputDate the date string to parse
     * @return the parsed LocalDate object
     * @throws DateTimeParseException if the input date string is invalid
     */
    private static LocalDate parseSpecificDate(String inputDate) throws DateTimeParseException {
        String[] parts = inputDate.split(" ");
        if (parts.length == 2) {
            int dayOfMonth = Integer.parseInt(parts[1].replaceAll("\\D+", ""));
            int month = parseMonth(parts[0]);
            int year = LocalDate.now().getYear();
            if (LocalDate.now().getMonthValue() > month || (LocalDate.now().getMonthValue() == month && LocalDate.now().getDayOfMonth() > dayOfMonth)) {
                year++;
            }
            return LocalDate.of(year, month, dayOfMonth);
        } else if (parts.length == 3) {
            int dayOfMonth = Integer.parseInt(parts[1].replaceAll("\\D+", ""));
            int month = parseMonth(parts[0]);
            int year = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, dayOfMonth);
        } else {
            throw new DateTimeParseException("Invalid input format", inputDate, 0);
        }
    }

    /**
     * Parses a month string into an integer.
     * 
     * @param monthString the month string to parse
     * @return the parsed month integer
     */
    public static int parseMonth(String monthString) {
        switch (monthString.toLowerCase(Locale.ENGLISH)) {
            case "jan":
            case "january":
                return 1;
            case "feb":
            case "february":
                return 2;
            case "mar":
            case "march":
                return 3;
            case "apr":
            case "april":
                return 4;
            case "may":
                return 5;
            case "jun":
            case "june":
                return 6;
            case "jul":
            case "july":
                return 7;
            case "aug":
            case "august":
                return 8;
            case "sep":
            case "september":
                return 9;
            case "oct":
            case "october":
                return 10;
            case "nov":
            case "november":
                return 11;
            case "dec":
            case "december":
                return 12;
            default:
                throw new DateTimeParseException("Invalid month", monthString, 0);
        }
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

    public static String calculateFutureDateTime(String timeString) {
        // Remove leading/trailing whitespace and convert to lowercase
        String input = timeString.trim().toLowerCase();

        // Extract numeric value and unit from the input string
        int value;
        String unit;
        if (input.contains("minute") || input.contains("min") || input.contains("mins") || input.contains("minutes")) {
            unit = "minute";
            value = extractNumericValue(input);
        } else if (input.contains("hour") || input.contains("hr") || input.contains("hrs") || input.contains("hours")) {
            unit = "hour";
            value = extractNumericValue(input);
        } else {
            throw new IllegalArgumentException("Invalid time unit, accepted formats are '5 mins', '3 hours', etc");
        }

        // Get the current LocalDateTime
        LocalTime now = LocalTime.now(ZoneId.of("-05:00")).plusHours(1);

        // Calculate the future LocalDateTime
        LocalTime futureTime;
        if (unit.equals("minute")) {
            futureTime = now.plusMinutes(value);
        } else { // unit.equals("hour")
            futureTime = now.plusHours(value);
        }

        // Return the future LocalDateTime as a string
        return futureTime.toString();
    }

    private static int extractNumericValue(String input) {
        // Remove non-digit characters
        String numericString = input.replaceAll("\\D+", "");
        return Integer.parseInt(numericString);
    }

    public static String convertSecondsToHoursMinutes(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hour" : " hours");
        }

        if (minutes > 0) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }

        if (remainingSeconds > 0) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(remainingSeconds).append(remainingSeconds == 1 ? " second" : " seconds");
        }

        return result.toString();
    }

}
