package com.georgster.util.commands;

import java.util.List;

import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser.ParseContext;

public class ParserRulesLogic {
    
    private ParserRulesLogic() {
        throw new UnsupportedOperationException("Utility class for a CommandParser");
    }

    protected static boolean isNumber(String arg) {
        try {
            Integer.parseInt(arg);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected static boolean isTime(String arg) {
        try {
            SoapUtility.timeConverter(arg);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    protected static boolean isDate(String arg) {
        try {
            SoapUtility.convertDate(arg);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    protected static boolean isFutureTimeIncrement(String arg) {
        try {
            SoapUtility.calculateFutureDateTime(arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected static boolean hasCharacter(String arg) {
        for (int i = 0; i < arg.length(); i++) {
            if (Character.isLetter(arg.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isOnlyCharacters(String arg) {
        for (int i = 0; i < arg.length(); i++) {
            if (!Character.isLetter(arg.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    protected static boolean isFirst(String arg, ParseContext parse) {
        return parse.getInputWords().indexOf(arg.split(" ")[0]) != 0;
    }

    protected static boolean isLast(String arg, ParseContext parse) {
        List<String> all = parse.getInputWords();
        return all.indexOf(arg.split(" ")[0]) != (all.size() - 1);
    }

    protected static boolean isMiddle(String arg, ParseContext parse) {
        List<String> all = parse.getInputWords();
        int argIndex = all.indexOf(arg.split(" ")[0]);
        return argIndex == 0 || argIndex == (all.size() - 1);
    }

    protected static boolean isBeforeIdentifier(String arg, ParseContext parse, List<String> identifiers) {
        List<String> all = parse.getInputWords();
        try {
            String[] argWords = arg.split(" ");
            int argIndex = all.indexOf(argWords[0]);
            int increment = argWords.length;
            return identifiers.contains(all.get(argIndex + increment));
        } catch (Exception e) {
            return false;
        }
    }

    protected static boolean isAfterIdentifier(String arg, ParseContext parse, List<String> identifiers) {
        List<String> all = parse.getInputWords();
        try {
            int argIndex = all.indexOf(arg.split(" ")[0]);
            return identifiers.contains(all.get(argIndex - 1));
        } catch (Exception e) {
            return false;
        }
    }


}
