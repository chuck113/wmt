package com.where.web;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.where.domain.Point;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.BranchIteratorImpl;
import com.where.tfl.grabber.ArrivalBoardScraper;
import com.where.tfl.grabber.TFLSiteScraper;
import com.where.stats.SingletonStatsCollector;

/**
 * eg: http://localhost:8080/rest/branches/jubilee
 * <p/>
 * url parameters:
 * <p/>
 * branch=<name>: the branch to iterate over
 * local=true: retrive train data from stored data - used for UI and disconnected testing
 *
 * Note the concurrency approach here won't work when mulitple jvms are used, it will have
 * to be done over some shared resource - datastore via memcache.
 */
public class BranchesResource extends WmtResource {

    private static final String EMPTY_JSON_POINTS_ARRAY = "{\"points\": { \"pointsArray\" : []}}";
    private static final String LOCAL_DATA = "local";

    private final Logger LOG = Logger.getLogger(BranchesResource.class);

    private String branchName;
    private String localParam;

    private ArrivalBoardScraper scraper = new TFLSiteScraper();
    private/* final*/ BranchIteratorSynchronizer branchSyncer;

    // conccurrency objects
//    private static final Map<String, BranchParseResult> RESULTS = new ConcurrentHashMap<String, BranchParseResult>();
//    private static final Map<String, Object> BRANCH_MUTEXES = new ConcurrentHashMap<String, Object>();
//
//    static {
//        for(String branch : WmtProperties.LINES_TO_ITERATE){
//            RESULTS.put(branch, new BranchParseResult());
//            BRANCH_MUTEXES.put(branch, new Object());
//        }
//    }


    public BranchesResource(){
        try {
            branchSyncer = PropsReader.buildBranchIteratorSynchronizerFactoryInstance().build(
                    new BranchIteratorImpl(getDaoFactory(), scraper));
        } catch (Exception e) {
            e.printStackTrace();
            branchSyncer = new DefaultBranchIteratorSynchronizerFactoryImpl().build( new BranchIteratorImpl(getDaoFactory(), scraper));
       }
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();        
        this.branchName = getRestPathAttribute(WmtRestApplication.BRANCH_URL_PATH_NAME);
        this.localParam = getQuery().getFirstValue(LOCAL_DATA);
    }


    /**
     * Returns a full representation for a given variant.
     */
    @Get//("json") //would be json but it means the text is downloaded as a file by browsers
    public String toJson() {
        try{
            //SingletonStatsCollector.getInstance().shallowHit(this.branchName);
            if (localParam != null) {
                return doLocalParamParse();
            }

            if (WmtProperties.CACHED_RESULT_PARSING){
                return branchSyncer.getBranch(this.branchName);
            }else {
                return doBranchParse();
            }
        }catch (Throwable e){
            e.printStackTrace();
            LOG.error(e);
            return EMPTY_JSON_POINTS_ARRAY;
        }
    }

    private String doLocalParamParse() {
        return getJsonPointsFromRecord(this.branchName);
    }

    private String doBranchParse() {
        try {
            LinkedHashMap<AbstractDirection, List<Point>> points;
            String result;

            ArrivalBoardScraper scraper = new TFLSiteScraper();
            points = new BranchIteratorImpl(getDaoFactory(), scraper).run(this.branchName);

            if (points != null) {
                result = ResultTransformer.toJson(new BranchIteratorImpl(getDaoFactory(), scraper).run(this.branchName));
            } else {
                result = "{ \"error\" : \" branch " + this.branchName + " is not known \"}";
            }

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.error(e);
            return "{\"error\" : \"" + e.getMessage() + "\" }";
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