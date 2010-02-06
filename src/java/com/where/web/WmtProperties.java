package com.where.web;

import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.Set;
import java.util.Collections;
import java.util.List;

/**
 * important settings
 */
public class WmtProperties {
    //public static final Set<String> LINES_TO_ITERATE = ImmutableSet.of("piccadilly");

    // ordered so the shorter ones are done first so if we have 3 servers there shouldn't be any delays
    public static final List<String> LINES_TO_ITERATE = Lists.newArrayList("victoria", "jubilee", "bakerloo","piccadilly","northern", "central");
    public static final String WEB_APP_CONTEXT = "rest/";
    public static final boolean CACHED_RESULT_PARSING = true;
    public static final boolean SHORTENED_JSON_LITERAL_NAMES = true;
    public static final long DATA_VALIDITY_PERIOD_MS = 50 * 1000;
    //public static final String EMPTY_JSON_POINTS_ARRAY = "{\"points\": { \"pointsArray\" : []}}";
    public static final String EMPTY_JSON_POINTS_ARRAY = "{\"p\": { \"a\" : []}}";
    public static final String INITIAL_POINTS_ENTRY = EMPTY_JSON_POINTS_ARRAY;
    public static final String LOCAL_RESULTS_CLASSPATH_FOLDER = "localresults";

}