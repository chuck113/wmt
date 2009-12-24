package com.where.web;

import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.Collections;

/**
 * important settings
 */
public class WmtProperties {
    //public static final Set<String> LINES_TO_ITERATE = ImmutableSet.of("piccadilly");
    public static final Set<String> LINES_TO_ITERATE = ImmutableSet.of("piccadilly", "central", "victoria", "jubilee", "bakerloo", "northern");
    public static final boolean CACHED_RESULT_PARSING = true;
    public static final boolean SHORTENED_JSON_LITERAL_NAMES = true;
    public static final long DATA_VALIDITY_PERIOD_MS = 50 * 1000;
    public static final String EMPTY_JSON_POINTS_ARRAY = "{\"points\": { \"pointsArray\" : []}}";
    public static final String INITIAL_POINTS_ENTRY = EMPTY_JSON_POINTS_ARRAY;

}
