package com.coreconsulting.res.openehr.tdd2rm.util;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class RegEx {

    public static String getFirstMatch(String string, String regex) {
        log.trace("getFirstMatch({}, {})", () -> string, () -> regex);
        Matcher matcher = Pattern.compile(regex).matcher(string);
        if (matcher.matches()) {
            String firstMatch = matcher.group(1);
            return firstMatch;
        } else {
            return null;
        }
    }

    public static List<String> getMatches(String string, String regex) {
        log.trace("getMatches({}, {})", () -> string, () -> regex);

        List<String> matches = new ArrayList<String>();

        Matcher matcher = Pattern.compile(regex).matcher(string);
        matcher.matches();

        int i = 0;
        while (i < matcher.groupCount())
            matches.add(matcher.group(++i));

        return matches;
    }

}
