package com.coreconsulting.res.openehr.tdd2canonical.util;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements functionality related to regular expressions, including group capturing.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class RegEx {

    /**
     * Returns the first captured group after matching a {@link String} against a regular expression.
     *
     * @param string text to match against the regular expression
     * @param regex regular expression
     * @return first captured group
     */
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

    /**
     * Returns all the captured groups after matching a {@link String} against a regular expression.
     *
     * @param string text to match against the regular expression
     * @param regex regular expression
     * @return {@link List} of captured groups
     */
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
