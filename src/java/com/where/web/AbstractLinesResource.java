package com.where.web;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.where.domain.Point;
import com.where.domain.DaoFactory;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.LineIteratorImpl;
import com.where.tfl.grabber.ArrivalBoardScraper;
import com.where.tfl.grabber.TFLSiteScraper;
import com.google.common.collect.SetMultimap;

/**
 * eg: http://localhost:8080/rest/branches/jubilee
 * <p/>
 * url parameters:
 * <p/>
 * branch=<name>: the branch to iterate over
 * local=true: retrive train data from stored data - used for UI and disconnected testing
 * <p/>
 * Note the concurrency approach here won't work when mulitple jvms are used, it will have
 * to be done over some shared resource - datastore via memcache.
 */
public abstract class AbstractLinesResource extends WmtResource {

    //private static final String EMPTY_JSON_POINTS_ARRAY = "{\"points\": { \"pointsArray\" : []}}";
    private static final String EMPTY_JSON_POINTS_ARRAY = "{\"p\": { \"a\" : []}}";
    private static final String LOCAL_DATA = "local";
    private static final String USE_JSONP = "jsonp";

    private static final String JSON_CALLBACK_FUNCTION_NAME = "jsoncallback";

    private final Logger LOG = Logger.getLogger(AbstractLinesResource.class);

    private String lineName;

    private LineIteratorImpl lineIterator;
    private boolean useJsonp;
    private boolean useLocalData;

    abstract LineIteratorSynchronizer getLineIteratorSynchronizer(LineIteratorImpl lineIterator);

    abstract DaoFactory getDaoFactory();

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.lineIterator = new LineIteratorImpl(getDaoFactory(), new TFLSiteScraper());
        this.lineName = getRestPathAttribute(WmtRestApplication.LINE_URL_PATH_NAME);
        this.useJsonp = getQueryParameter(USE_JSONP) != null;
        this.useLocalData = getQueryParameter(LOCAL_DATA) != null;
    }


    /**
     * Returns a full representation for a given variant.
     */
    @Get
    //("json") //would be json but it means the text is downloaded as a file by browsers
    public String toJson() {
        String jsonResult = process();
        if (useJsonp) {
            return getQueryParameter(JSON_CALLBACK_FUNCTION_NAME) + "(" + jsonResult + ");";
        } else {
            return jsonResult;
        }
    }

    private String process() {
        try {
            if (useLocalData) {
                return doLocalParamParse();
            }else if (WmtProperties.CACHED_RESULT_PARSING) {
                return getLineIteratorSynchronizer(lineIterator).getLine(this.lineName);
            } else {
                return doLineParse();
            }
        } catch (Throwable e) {
            LOG.error(e);
            return EMPTY_JSON_POINTS_ARRAY;
        }
    }

    private String doLocalParamParse() {
        return getJsonPointsFromRecord(this.lineName);
    }

    private String doLineParse() {
        try {
            if(!WmtProperties.LINES_TO_ITERATE.contains(this.lineName)){
                return JsonTransformer.toJsonError("line " + this.lineName + " is not known");
            }

            return JsonTransformer.toJson(lineIterator.run(this.lineName));
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.error(e);
            return JsonTransformer.toJsonError(e.getMessage());
        }
    }

    private String getJsonPointsFromRecord(final String branch) {
        String resourcePath = WmtProperties.LOCAL_RESULTS_CLASSPATH_FOLDER+"/" + branch + ".json";
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            LOG.warn("Couldn't load recored resource: '" + resourcePath + "'");
            return (EMPTY_JSON_POINTS_ARRAY);
        }

        try {
            return IOUtils.toString(stream);
        } catch (IOException e) {
            LOG.warn("Couldn't load recored resource: '" + resourcePath + "'");
            return (EMPTY_JSON_POINTS_ARRAY);
        }

    }
}