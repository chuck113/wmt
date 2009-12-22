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

    private static final String EMPTY_JSON_POINTS_ARRAY = "{\"points\": { \"pointsArray\" : []}}";
    private static final String LOCAL_DATA = "local";

    private final Logger LOG = Logger.getLogger(AbstractLinesResource.class);

    private String lineName;
    private String localParam;

    private final ArrivalBoardScraper scraper = new TFLSiteScraper();

    abstract LineIteratorSynchronizer getLineIteratorSynchronizer(ArrivalBoardScraper scraper, DaoFactory daoFactory);
    abstract DaoFactory getDaoFactory();

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.lineName = getRestPathAttribute(WmtRestApplication.LINE_URL_PATH_NAME);
        this.localParam = getQuery().getFirstValue(LOCAL_DATA);
    }


    /**
     * Returns a full representation for a given variant.
     */
    @Get
    //("json") //would be json but it means the text is downloaded as a file by browsers
    public String toJson() {
        try {
            //SingletonStatsCollector.getInstance().shallowHit(this.lineName);
            if (localParam != null) {
                return doLocalParamParse();
            }

            if (WmtProperties.CACHED_RESULT_PARSING) {
                return getLineIteratorSynchronizer(scraper, getDaoFactory()).getLine(this.lineName);
            } else {
                return doLineParse();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.error(e);
            return EMPTY_JSON_POINTS_ARRAY;
        }
    }

    private String doLocalParamParse() {
        return getJsonPointsFromRecord(this.lineName);
    }

    private String doLineParse() {
        try {
            SetMultimap<AbstractDirection, Point> points;
            String result;

            ArrivalBoardScraper scraper = new TFLSiteScraper();
            points = new LineIteratorImpl(getDaoFactory(), scraper).run(this.lineName);

            if (points != null) {
                result = JsonTransformer.toJson(new LineIteratorImpl(getDaoFactory(), scraper).run(this.lineName));
            } else {
                result = JsonTransformer.toJsonError("line " + this.lineName + " is not known");
            }

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.error(e);
            return JsonTransformer.toJsonError(e.getMessage());
        }
    }

    private String getJsonPointsFromRecord(final String branch) {
        String resourcePath = "localresults/" + branch + ".json";
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