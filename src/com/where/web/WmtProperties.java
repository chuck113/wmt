package com.where.web;

import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * important settings
 */
public class WmtProperties {

    static{
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            System.out.println("WmtProperties.static intializer "+stackTraceElement);
        }
    }

    public static final Set<String> LINES_TO_ITERATE = ImmutableSet.of("victoria", "jubilee", "bakerloo");
    public static final boolean CACHED_RESULT_PARSING = true;
    public static final long DATA_VALIDITY_PERIOD_MS = 50 * 1000;
}
