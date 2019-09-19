package com.coreconsulting.res.openehr;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RegEx {

    public static String getFirstMatch(String string, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(string);
        if (matcher.matches())
            return matcher.group(1);
        else
            return null;
    }

    public static List<String> getMatches(String string, String regex) {
        List<String> matches = new ArrayList<String>();

        Matcher matcher = Pattern.compile(regex).matcher(string);
        matcher.matches();

        int i = 0;
        while (i < matcher.groupCount())
            matches.add(matcher.group(++i));

        return matches;
    }

}
