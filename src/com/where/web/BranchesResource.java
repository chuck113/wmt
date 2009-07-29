package com.where.web;

import org.restlet.resource.*;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.where.domain.Point;
import com.where.domain.Direction;
import com.where.domain.alg.Algorithm;
import com.where.tfl.grabber.TFLSiteScraper;

import java.util.*;
import java.io.*;
import java.text.DateFormat;

/**
 * eg: http://localhost:8080/rest/branches/jubilee
 */
public class BranchesResource extends WmtResource {

    private static final Set<String> BRANCHES = new HashSet<String>(Arrays.asList(new String[]{
            "Victoria"
    }));

    private static final String SAVED_POINTS_FOLDER = "/recorded";

    private static final String TEST_MODE_URL_PARAM_NAME = "testMode";
    private static final String REPLAY_URL_PARAM_NAME = "replay";

    private static final String CACHE_KEY_SUFFIX = "last_run";

    private final Logger LOG = Logger.getLogger(BranchesResource.class);

    private final String branchName;
    private final String recordedPointsFolder;
    private final String testModeParam;
    private final String replayParam;

    public BranchesResource(Context context, Request request, Response response) {
        super(context, request, response);

        recordedPointsFolder = getServletContext().getRealPath(WEB_INF+SAVED_POINTS_FOLDER);

        // This representation has only one type of representation.
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        this.branchName = getRestPathAttribute(WmtRestApplication.BRANCH_URL_PATH_NAME);
        this.testModeParam = getQuery().getFirstValue(TEST_MODE_URL_PARAM_NAME);
        this.replayParam = getQuery().getFirstValue(REPLAY_URL_PARAM_NAME);
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Set<Point> points;
        String result;

        LOG.info("Call to branch. Branch is '"+this.branchName+"', testmode is: '"+testModeParam+"'");

        if (branchName.equals("test")) {
            points = new HashSet<Point>(Arrays.asList(
                    new Point(51.5173, -0.1246, Direction.DirectionEnum.NORTHBOUND, ""),
                    new Point(51.5183, -0.1246, Direction.DirectionEnum.NORTHBOUND, ""),
                    new Point(51.5193, -0.1246, Direction.DirectionEnum.SOUTHBOUND, "")));
        } else if (replayParam != null) {
            points = getCache().get(makeCacheKey());
            if(points == null)points = Collections.emptySet();
        } else if (testModeParam != null && testModeParam.equals("1")) {
            return returnAsJson(getJsonPointsFromRecord(this.branchName));
        } else {
            points = new Algorithm(this.branchName,getDataMapper(), new TFLSiteScraper()).run();
            //points = getPointsFromRecord(branchName);
            getCache().put(makeCacheKey(), points);

            serializePoints(points, branchName);
        }

        if (points != null) {
            result = makeJsonPoints(points).toString();
        } else {
            result = "{ \"error\" : \" branch " + this.branchName + " is not known \"}";
        }

        return returnAsJson(result);

    }

    private String makeCacheKey(){
        return this.branchName+"-"+CACHE_KEY_SUFFIX;
    }

    private StringBuffer getJsonPointsFromRecord(final String branch) {
        File recordedRoot = new File(recordedPointsFolder);
        String[] files = recordedRoot.list(new FilenameFilter() {
            public boolean accept(File file, String s) {
                System.out.println("BranchesResource.accept file: " + file + ", s: " + s);
                return s.startsWith(branch);
            }
        });


        if (files == null || files.length == 0) {
            LOG.warn("found no recored  ponits for branch "+branch);
            return new StringBuffer("{\"points\": { \"pointsArray\" : []}}\n");
        }
        File fileToUse = new File(recordedPointsFolder, files[0]);
        LOG.info("Deserialising file: " + fileToUse);

        try {
            return new StringBuffer(IOUtils.toString(new FileInputStream(fileToUse)));
        } catch (Exception e) {
            LOG.error("failed to deserialize file: " + fileToUse, e);
            return null;
        }
    }

    private Set<Point> getPointsFromRecord(final String branch) {
        File recordedRoot = new File(recordedPointsFolder);
        String[] files = recordedRoot.list(new FilenameFilter() {
            public boolean accept(File file, String s) {
                System.out.println("BranchesResource.accept file: " + file + ", s: " + s);
                return s.startsWith(branch);
            }
        });


        if (files.length == 0) return Collections.emptySet();
        File fileToUse = new File(recordedPointsFolder, files[0]);
        LOG.info("Deserialising file: " + fileToUse);

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileToUse));
            return (Set<Point>) in.readObject();
        } catch (Exception e) {
            LOG.error("failed to deserialize file: " + fileToUse, e);
            return null;
        }
    }

    private void serializePoints(Set<Point> points, String branch) {
        try {
            String date = DateFormat.getDateTimeInstance().format(new Date()).replace(':', '-').replace(' ', '-');

            File jsonFile = new File(recordedPointsFolder, branch + "-" + date + ".json");
            LOG.info("writing points to record file: "+jsonFile +", can write: "+jsonFile.canWrite());
            FileUtils.forceMkdir(new File(recordedPointsFolder));
            jsonFile.createNewFile();

            IOUtils.write(makeJsonPoints(points),new FileOutputStream(jsonFile));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
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
    private StringBuffer makeJsonPoints(Set<Point> points) {
        StringBuffer buf = new StringBuffer("{\"points\": { \"pointsArray\" : [\n");

        for (Point point : points) {
            buf.append("  { \"lat\" : " + point.getLat() + ", \"lng\" : " + point.getLng() + ", \"direction\" : \""+point.getDirection().getName()+"\", \"description\" : \""+point.getDescription()+"\"},\n");
        }
        buf.append("]}}");
        return buf;
    }
}