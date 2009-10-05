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
import com.where.domain.BranchStop;
import com.where.domain.alg.Algorithm;
import com.where.domain.alg.AbstractDirection;
import com.where.tfl.grabber.TFLSiteScraper;

import java.util.*;
import java.io.*;
import java.text.DateFormat;

/**
 * eg: http://localhost:8080/rest/branches/jubilee
 *
 * url parameters:
 *
 * branch=<name>: the branch to iterate over
 * local=true: retrive train data from stored data - used for UI and disconnected testing
 */
public class BranchesResource extends WmtResource {

    private static final String SAVED_POINTS_FOLDER = "/recorded";
    private static final String LOCAL_DATA = "local";
    private static final long LOCAL_DATA_SLEEP_TIME = 3000;

    private static final String CACHE_KEY_SUFFIX = "last_run";

    private final Logger LOG = Logger.getLogger(BranchesResource.class);

    private final String branchName;
    private final String recordedPointsFolder;
    //private final String testModeParam;
    private final String localParam;


    public BranchesResource(Context context, Request request, Response response) {
        super(context, request, response);

        recordedPointsFolder = getServletContext().getRealPath(SAVED_POINTS_FOLDER);

        // This representation has only one type of representation.
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        this.branchName = getRestPathAttribute(WmtRestApplication.BRANCH_URL_PATH_NAME);
        this.localParam = getQuery().getFirstValue(LOCAL_DATA);
         getQuery().getQueryString();
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        LinkedHashMap<AbstractDirection, List<Point>> points;
        String result;

        /*if (branchName.equals("test")) {
            points = new LinkedHashMap<AbstractDirection, List<Point>>();
            points.put(AbstractDirection.ONE, Arrays.asList(
                    new Point(51.5173, -0.1246, Direction.DirectionEnum.NORTHBOUND, ""),
                    new Point(51.5183, -0.1246, Direction.DirectionEnum.NORTHBOUND, ""),
                    new Point(51.5193, -0.1246, Direction.DirectionEnum.SOUTHBOUND, "")));
        } else if (replayParam != null) {
            points = getCache().get(makeCacheKey());
            if(points == null)points = new LinkedHashMap<AbstractDirection, List<Point>>();
        } else */if (localParam != null) {
            return returnAsJson(getJsonPointsFromRecord(this.branchName));
        } else {
            TFLSiteScraper scraper = new TFLSiteScraper();
            points = new Algorithm(this.branchName,getDaoFactory(), scraper).run();
            //String boardsHtmlPage = makeHtmlOfParsedBoards(scraper.getHtmlTables(), points);
             getCache().put(makeCacheKey(), points);
            serializePoints(points, branchName);
//            if(lastResult != null) {
//                String comparision = makePointsComparisionTable(lastResult, points);
//            }
            //lastResult = points;
        }

        if (points != null) {
            result = makeJsonPoints(convertPoints(points)).toString();
        } else {
            result = "{ \"error\" : \" branch " + this.branchName + " is not known \"}";
        }

        return returnAsJson(result);
    }

     private String makeHtmlOfParsedBoards(Map<BranchStop, String> htmlchucks, LinkedHashMap<AbstractDirection, List<Point>> points) {
        StringBuilder bulider = new StringBuilder("<html><head></head><body>\n<p>Parsed html</p>");
         for(BranchStop stop : htmlchucks.keySet()){
            bulider.append("<p>"+stop.getStation().getName()+"</p>\n");
             bulider.append(htmlchucks.get(stop)+"</br>\n");
         }

         bulider.append("<p>actual points recorded:</p>\n");
         for(AbstractDirection dir : points.keySet()){
             List<Point> pointsDir = points.get(dir);
             bulider.append("<p>direction "+dir+"</p>\n");

             for(Point point: pointsDir){
                bulider.append(point.getDescription()+"</br>\n"); 
             }
         }

         bulider.append("</body><html>");
         //System.out.println("BranchesResource.makeHtmlOfParsedBoards html is\n"+bulider.toString());

//         File fileToUse = new File(recordedPointsFolder, "dump-"+System.currentTimeMillis()+".txt");
//         try{
//            IOUtils.write(bulider.toString(), new FileWriter(fileToUse));
//         }catch(Exception e){
//             e.printStackTrace();
//         }
         return bulider.toString();
     }

    private String makeCacheKey(){
        return this.branchName+"-"+CACHE_KEY_SUFFIX;
    }

    private StringBuffer getJsonPointsFromRecord(final String branch) {
        File recordedRoot = new File(recordedPointsFolder);
        String[] files = recordedRoot.list(new FilenameFilter() {
            public boolean accept(File file, String s) {
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
            Thread.sleep(LOCAL_DATA_SLEEP_TIME);
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

    private String comparePoints(LinkedHashMap<AbstractDirection, List<Point>> lastParse, LinkedHashMap<AbstractDirection, List<Point>> thisParse){
        return null;
    }

    private String makePointsComparisionTable(LinkedHashMap<AbstractDirection, List<Point>> lastParse, LinkedHashMap<AbstractDirection, List<Point>> thisParse){
        Map<AbstractDirection, List<String>> lastDescriptions;
        Map<AbstractDirection, List<String>> thisDescriptions;

        int longestString = 0;

        for(AbstractDirection dir : lastParse.keySet()){
            List<Point> list = lastParse.get(dir);
            for (Point point : list) {
                String s = point.getDescription();
                if (s.length() > longestString){
                   longestString = s.length(); 
                }
            }
        }

        lastDescriptions = makeList(lastParse, longestString);
        thisDescriptions = makeList(thisParse, longestString);

        Iterator<AbstractDirection> directionIterator = lastDescriptions.keySet().iterator();
        StringBuilder builder = new StringBuilder();

        while(directionIterator.hasNext()){
            AbstractDirection direction = directionIterator.next();
            Iterator<String> lastDescList = lastDescriptions.get(direction).iterator();
            Iterator<String> thisDescList = thisDescriptions.get(direction).iterator();

            while(lastDescList.hasNext() && thisDescList.hasNext()){
                builder.append("| "+ lastDescList.next() +" | "+thisDescList.next()+" \n");
            }

            for(int i=0; i< ((longestString*2)+5); i++){
                builder.append("-");
            }
            builder.append("\n");
        }

        return builder.toString();
    }


    private Map<AbstractDirection, List<String>> makeList(LinkedHashMap<AbstractDirection, List<Point>> lastParse, int longestString) {
        Map<AbstractDirection, List<String>> result = new HashMap<AbstractDirection, List<String>>();

        for(AbstractDirection dir : lastParse.keySet()){
            List<Point> list = lastParse.get(dir);
            List<String> res = new ArrayList<String>();
            for (Point point : list) {
               res.add(stringWithPadding(point.getDescription(), longestString));
            }
            result.put(dir, res);
        }
        return result;
    }

    private String stringWithPadding(String st, int targetLength){
        StringBuilder res = new StringBuilder(st);

        for(int i=0; i<(targetLength - st.length()); i++)res.append(" ");
        return res.toString();
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

    private void serializePoints(LinkedHashMap<AbstractDirection, List<Point>> points, String branch){
        serializePoints(convertPoints(points), branch);
    }

     private Set<Point> convertPoints(LinkedHashMap<AbstractDirection, List<Point>> points){
       Set<Point> result = new HashSet<Point>();

        for(List<Point> ps : points.values()){
          result.addAll(ps);
        }
        return result;
    }

    private StringBuffer makeJsonPoints(Set<Point> points) {
        StringBuffer buf = new StringBuffer("{\"points\": { \"pointsArray\" : [\n");

        for (Point point : points) {
            buf.append("  { \"lat\" : " + point.getLat() + ", \"lng\" : " + point.getLng() + ", \"direction\" : \""+point.getDirection().getName()+"\", \"description\" : \""+point.getDescription()+"\"},\n");
        }
        buf.append("]}}");
        return buf;
    }
}