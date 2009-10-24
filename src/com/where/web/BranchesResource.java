package com.where.web;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.where.domain.BranchStop;
import com.where.domain.Point;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.BranchIterator;
import com.where.tfl.grabber.ArrivalBoardScraper;
import com.where.tfl.grabber.TFLSiteScraper;

/**
 * eg: http://localhost:8080/rest/branches/jubilee
 * <p/>
 * url parameters:
 * <p/>
 * branch=<name>: the branch to iterate over
 * local=true: retrive train data from stored data - used for UI and disconnected testing
 */
public class BranchesResource extends WmtResource {

    private static final String EMPTY_JSON_POINTS_ARRAY = "{\"points\": { \"pointsArray\" : []}}";
    private static final String LOCAL_DATA = "local";

    private final Logger LOG = Logger.getLogger(BranchesResource.class);

    private String branchName;
    private String localParam;

    // conccurrency objects
    private static final Map<String, Result> RESULTS = new ConcurrentHashMap<String, Result>();
    private static final Map<String, Object> BRANCH_MUTEXES = new ConcurrentHashMap<String, Object>();

    static {
        for(String branch : WmtProperties.LINES_TO_ITERATE){
            RESULTS.put(branch, new Result());
            BRANCH_MUTEXES.put(branch, new Object());
        }

//        RESULTS.put("jubilee", new Result());
//        RESULTS.put("victoria", new Result());
//
//        BRANCH_MUTEXES.put("jubilee", new Object());
//        BRANCH_MUTEXES.put("victoria", new Object());
    }


    private static class Result {
        private long recordedAt = 0;
        private CharSequence result = null;
        private boolean parseInProgress = false;

        public void update(long recordedAt, CharSequence result, boolean parseInProgress) {
            this.recordedAt = recordedAt;
            this.result = result;
            this.parseInProgress = parseInProgress;
        }

        public long getRecordedAt() {
            return recordedAt;
        }

        public void setRecordedAt(long recordedAt) {
            this.recordedAt = recordedAt;
        }

        public CharSequence getResult() {
            return result;
        }

        public void setResult(CharSequence result) {
            this.result = result;
        }

        public boolean isParseInProgress() {
            return parseInProgress;
        }

        public void setParseInProgress(boolean parseInProgress) {
            this.parseInProgress = parseInProgress;
        }

        public boolean isValid() {
            System.out.println("BranchesResource$LastResult.isValid checking if " + new Date(recordedAt + WmtProperties.DATA_VALIDITY_PERIOD_MS) + " > " + new Date());
            return (recordedAt + WmtProperties.DATA_VALIDITY_PERIOD_MS) > System.currentTimeMillis();
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
    @Get("json")
    public String toJson() {

        if (localParam != null) {
            return doLocalParamParse();
        }

        if (WmtProperties.CACHED_RESULT_PARSING)
            return doSynchronizedCachedResultParse();
        else {
            return doBranchParse();
        }
    }

    private String doLocalParamParse() {
        return getJsonPointsFromRecord(this.branchName);
    }

    private String doBranchParse() {
        try {
            LinkedHashMap<AbstractDirection, List<Point>> points;
            String result;
            LOG.debug("BranchesResource.doBranchParse memory before Parse: \n" + makeMemUsageString());

            ArrivalBoardScraper scraper = new TFLSiteScraper();
            points = new BranchIterator(this.branchName, getDaoFactory(), scraper).run();

            LOG.debug("BranchesResource.doBranchParse memory After Parse: \n" + makeMemUsageString());

            if (points != null) {
                result = makeJsonPoints(convertPoints(points)).toString();
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

    private String makeMemUsageString() {
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usage = totalMem - freeMem;

        StringBuilder res = new StringBuilder();
        res.append("totalMem: " + totalMem + "\n");
        res.append("freeMem: " + freeMem + "\n");
        res.append("usageMem: " + usage + "\n");
        return res.toString();
    }

    private void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException("rethrowing" + e);
        }
    }

    /**
     * This class holds a cache of recently parsed points as json that is retunred. The points
     * are valid for a certain amount of time. If the points are valid they are returned,
     * if they are not valid we do a parse and updated the cached points. If another
     * thread comes along while the parse is happening, the current, invalid points
     * are returned. If on the first parse another thread comes along it waits for
     * the parse to finish before returning.
     * <p/>
     * Returning the invalid result is simpler than waiting for the existing parse to
     * finish as there is less moving parts and the client gets results faster but
     * the client also will see out of date data for longer.
     */
    private String doSynchronizedCachedResultParse() {
        Result resultObj = RESULTS.get(branchName);

        if (resultObj.isValid()) {
            safeSleep(3000);
            return resultObj.getResult().toString();
        } else {
            LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " result is " + resultObj.getResult() + " at " + new Date());
            if (resultObj.getResult() == null) {//this will happen on the first parse before a result is created
                LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " no result, entering mutex at " + new Date());
                synchronized (BRANCH_MUTEXES.get(branchName)) {
                    if (resultObj.getResult() == null) {
                        resultObj.setParseInProgress(true);
                        System.out.println("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " entered mutex and parsing...");
                        CharSequence resultJson = doBranchParse();
                        resultObj.update(System.currentTimeMillis(), resultJson, false);
                        LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " exiting mutex after running alg at " + new Date());
                        return resultJson.toString();
                    } else {
                        LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " exiting mutex at " + new Date());
                        return resultObj.toString();
                    }
                }
            } else if (resultObj.isParseInProgress()) {
                LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " json no longer valid, returning old result");
                // return the invalid result
                safeSleep(3000);
                return resultObj.getResult().toString();
            } else {
                LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " json no longer valid, reparsing...");
                resultObj.setParseInProgress(true);
                CharSequence resultJson = doBranchParse();
                //FIXME there is no synchronization when udpating the object, should there be?
                resultObj.update(System.currentTimeMillis(), resultJson, false);
                return resultJson.toString();
            }
        }
    }


    private String makeHtmlOfParsedBoards(Map<BranchStop, String> htmlchucks, LinkedHashMap<AbstractDirection, List<Point>> points) {
        StringBuilder bulider = new StringBuilder("<html><head></head><body>\n<p>Parsed html</p>");
        for (BranchStop stop : htmlchucks.keySet()) {
            bulider.append("<p>" + stop.getStation().getName() + "</p>\n");
            bulider.append(htmlchucks.get(stop) + "</br>\n");
        }

        bulider.append("<p>actual points recorded:</p>\n");
        for (AbstractDirection dir : points.keySet()) {
            List<Point> pointsDir = points.get(dir);
            bulider.append("<p>direction " + dir + "</p>\n");

            for (Point point : pointsDir) {
                bulider.append(point.getDescription() + "</br>\n");
            }
        }

        bulider.append("</body><html>");
        return bulider.toString();
    }

    private String getJsonPointsFromRecord(final String branch) {
        String resourcePath = "recorded/" + branch + ".json";
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

    private String comparePoints(LinkedHashMap<AbstractDirection, List<Point>> lastParse, LinkedHashMap<AbstractDirection, List<Point>> thisParse) {
        return null;
    }

    private String makePointsComparisionTable(LinkedHashMap<AbstractDirection, List<Point>> lastParse, LinkedHashMap<AbstractDirection, List<Point>> thisParse) {
        Map<AbstractDirection, List<String>> lastDescriptions;
        Map<AbstractDirection, List<String>> thisDescriptions;

        int longestString = 0;

        for (AbstractDirection dir : lastParse.keySet()) {
            List<Point> list = lastParse.get(dir);
            for (Point point : list) {
                String s = point.getDescription();
                if (s.length() > longestString) {
                    longestString = s.length();
                }
            }
        }

        lastDescriptions = makeList(lastParse, longestString);
        thisDescriptions = makeList(thisParse, longestString);

        Iterator<AbstractDirection> directionIterator = lastDescriptions.keySet().iterator();
        StringBuilder builder = new StringBuilder();

        while (directionIterator.hasNext()) {
            AbstractDirection direction = directionIterator.next();
            Iterator<String> lastDescList = lastDescriptions.get(direction).iterator();
            Iterator<String> thisDescList = thisDescriptions.get(direction).iterator();

            while (lastDescList.hasNext() && thisDescList.hasNext()) {
                builder.append("| " + lastDescList.next() + " | " + thisDescList.next() + " \n");
            }

            for (int i = 0; i < ((longestString * 2) + 5); i++) {
                builder.append("-");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public String tryAlternateSite(String branch){
        try {
            URL url = new URL("http://wheresmytube.com/servlet/rest/branches/"+branch);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(3000);
            InputStream inputStream = urlConnection.getInputStream();
            return IOUtils.toString(inputStream);
        } catch (MalformedURLException e) {
           return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Map<AbstractDirection, List<String>> makeList(LinkedHashMap<AbstractDirection, List<Point>> lastParse, int longestString) {
        Map<AbstractDirection, List<String>> result = new HashMap<AbstractDirection, List<String>>();

        for (AbstractDirection dir : lastParse.keySet()) {
            List<Point> list = lastParse.get(dir);
            List<String> res = new ArrayList<String>();
            for (Point point : list) {
                res.add(stringWithPadding(point.getDescription(), longestString));
            }
            result.put(dir, res);
        }
        return result;
    }

    private String stringWithPadding(String st, int targetLength) {
        StringBuilder res = new StringBuilder(st);

        for (int i = 0; i < (targetLength - st.length()); i++) res.append(" ");
        return res.toString();
    }

//    private void serializePoints(Set<Point> points, String branch) {
//        try {
//            String date = DateFormat.getDateTimeInstance().format(new Date()).replace(':', '-').replace(' ', '-');
//
//            File jsonFile = new File(recordedPointsFolder, branch + "-" + date + ".json");
//            LOG.info("writing points to record file: " + jsonFile + ", can write: " + jsonFile.canWrite());
//            FileUtils.forceMkdir(new File(recordedPointsFolder));
//            jsonFile.createNewFile();
//
//            IOUtils.write(makeJsonPoints(points), new FileOutputStream(jsonFile));
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//{"menu": {
//  "id": "file",
//  "value": "File",
//  "popup": {
//    "menuitem": [
//      {"value": "New", "onclick": "CreateNewDoc()"},
//      {"value": "Open", "onclick": "OpenDoc()"},
//      {"value": "Close", "onclick": "CloseDoc()"}
//    ]
//  }

    //}}
    // "points": [{"x" : "xx"}, {"x" : "xx"}]

//    private void serializePoints(LinkedHashMap<AbstractDirection, List<Point>> points, String branch) {
//        serializePoints(convertPoints(points), branch);
//    }

    private Set<Point> convertPoints(LinkedHashMap<AbstractDirection, List<Point>> points) {
        Set<Point> result = new HashSet<Point>();

        for (List<Point> ps : points.values()) {
            result.addAll(ps);
        }
        return result;
    }

    private StringBuffer makeJsonPoints(Set<Point> points) {
        StringBuffer buf = new StringBuffer("{\"points\": { \"pointsArray\" : [\n");

        for (Point point : points) {
            buf.append("  { \"lat\" : " + point.getLat() + ", \"lng\" : " + point.getLng() + ", \"direction\" : \"" + point.getDirection().getName() + "\", \"description\" : \"" + point.getDescription() + "\"},\n");
        }
        buf.append("]}}");
        return buf;
    }
}