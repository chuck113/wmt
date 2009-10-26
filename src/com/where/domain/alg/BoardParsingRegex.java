package com.where.domain.alg;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @deprecated not in use
 *
 * @author Charles Kubicek
 */
public class BoardParsingRegex {

    private final String atPlatformRegex = "\\A[At ]([A-Za-z0-9 ']*)"; // for "At Kilburn Park platform 2";
    private final String platformRegex = "\\A[A-Za-z ]* [pP]latform (.*)"; // for Kilburn Park platform 2

    //BETWEEN_AND("Between", " and")
    //private final String betweenAndRegex = "\\ABetween ([A-Za-z0-9 ]*) [Aa]nd ([A-Za-z0-9 ]*)";
    private final String betweenAndRegex = "\\ABetween ([A-Za-z0-9 ']*) and ([A-Za-z0-9 ']*)";

    private void betweenAnd() {
        Pattern pattern = Pattern.compile(betweenAndRegex);
        {
            String input1 = "Between Queen's Park and Kilburn Park";
            Matcher matcher = pattern.matcher(input1);

            int i = 0;
            while (matcher.find()) {
                int start = matcher.start(i);

                // Get ending position

                int end = matcher.end(i);

                // Print whatever matched.

                System.out.println("start=" + start + "; end=" + end);

                String result = matcher.group(i);
                System.out.println(betweenAndRegex + " 1 matches '" + result + "'");
                i++;
            }
        }
    }

    private void platform() {
        Pattern pattern = Pattern.compile(platformRegex);
        {
            String input1 = "Kilburn Park Platform 2";
            Matcher matcher = pattern.matcher(input1);

            if (matcher.find()) {
                String result = matcher.group(0);
                System.out.println(platformRegex + " 1 matches '" + result + "'");
            }
        }
    }

    private void at() {
        Pattern pattern = Pattern.compile(atPlatformRegex);
        {
            String input1 = "At Kilburn Park platform 2";
            Matcher matcher = pattern.matcher(input1);

            if (matcher.find()) {
                String result = matcher.group(0);
                System.out.println(atPlatformRegex + " 1 matches '" + result.substring(3, result.length()) + "'");
            }
        }
        {
            String input2 = "Kilburn Park platform 2";
            Matcher matcher = pattern.matcher(input2);

            if (matcher.find()) {
                System.out.println(atPlatformRegex + " 2 matches '" + matcher.group(0) + "'");
            }
        }
    }

    public static void main(String[] args) {
        //new BoardParsingRegex().platform();
        //new BoardParsingRegex().at();
        new BoardParsingRegex().betweenAnd();
    }
}
