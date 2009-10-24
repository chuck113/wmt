package com.where.core;

import junit.framework.TestCase;
import com.where.testtools.TflSiteScraperFromSavedFilesForTesting;
import com.where.domain.alg.BranchIterator;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.Point;

import java.io.File;
import java.util.*;

/**
 * @author Charles Kubicek
 */
public class LongevityRecordedTests extends TestCase {

        private final String htmlsFolder = "C:\\data\\projects\\wheresmytube\\recorded-long\\";
    private final WhereFixture fixture = new WhereFixture();


    private List<LinkedHashMap<AbstractDirection, List<Point>>> run(String folder, String branchName){
        List<LinkedHashMap<AbstractDirection, List<Point>>> res = new ArrayList<LinkedHashMap<AbstractDirection, List<Point>>>();

        File[] files = new File(folder).listFiles();

        /*
         * Holds the number of the parse mapped to the station name mapped to the file for
         * that station
         */
        Map<Integer, List<String>> biDirectionalParseFolders = new HashMap<Integer, List<String>>();
        for(File f: files){
            int individualParseId = new Integer(f.getName().split("-")[0]);
            String stationNameWithDotTxt = (f.getName().split("-")[2]);
            String stationName = stationNameWithDotTxt.substring(0, (stationNameWithDotTxt.length()-4));

            if(!biDirectionalParseFolders.containsKey(individualParseId)){
                biDirectionalParseFolders.put(individualParseId, new ArrayList<String>());
            }
            List<String> stationNames= biDirectionalParseFolders.get(individualParseId);
            stationNames.add(stationName);
        }

        List<List<String>> parseFiles = new ArrayList<List<String>>();
        for(int i=0; i<biDirectionalParseFolders.size(); i++){
            parseFiles.add(biDirectionalParseFolders.get(i));
        }

        for (int i=0; i< parseFiles.size(); i++) {
            System.out.println("LongevityRecordedTests.run parsing id: "+i);
                TflSiteScraperFromSavedFilesForTesting scraper = new TflSiteScraperFromSavedFilesForTesting(new File(folder), ""+i);
                BranchIterator branchIterator = new BranchIterator(branchName, fixture.getSerializedFileDaoFactory(), scraper);
                res.add(branchIterator.run());
        }

        return res;
    }

    public void test_2009_10_17__17_52_54() throws Exception{
        String branchName = "victoria";
        String folder = htmlsFolder+branchName+"__2009_10_17__17_52_54";
        List<LinkedHashMap<AbstractDirection, List<Point>>> results = run(folder, branchName);

        AbstractDirection dir = results.get(0).keySet().iterator().next();

        Iterator<Point> points1 = results.get(0).get(dir).iterator();
        Iterator<Point> points2 = results.get(1).get(dir).iterator();

        while(points1.hasNext() && points2.hasNext()) {
           Point point1 = points1.next();
           Point point2 = points2.next();

            System.out.println(point1.getDescription() +" -- "+point2.getDescription());
        }
    }

    private void assertNoDups(List<Point> points){
        Set<String> found = new HashSet<String>();

        for (Point point : points) {
            if(found.contains(point)){
                fail("contains duplicate: "+point);
            } else {
                found.add(point.getDescription());
            }
        }
    }
}
