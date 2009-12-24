package com.where.longrunning;

import junit.framework.TestCase;
import com.where.testtools.TflSiteScraperFromSavedFilesForTesting;
import com.where.domain.alg.BranchIteratorImpl;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.DiscoveredTrain;
import com.where.domain.alg.BranchIterator;
import com.where.domain.Point;
import com.where.core.WhereFixture;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

/**
 * @author Charles Kubicek
 */
public class LongevityRecordedTests extends TestCase {

    private final String htmlsFolder = WhereFixture.HTMLS_LONG_FOLDER;
    private final WhereFixture fixture = new WhereFixture();

    private List<SetMultimap<AbstractDirection, Point>> run(String folder, String branchName) {
        List<SetMultimap<AbstractDirection, Point>> res = new ArrayList<SetMultimap<AbstractDirection, Point>>();

        File[] files = new File(folder).listFiles();

        /*
         * Holds the number of the parse mapped to the station name mapped to the file for
         * that station
         */
        Map<Integer, List<String>> biDirectionalParseFolders = new HashMap<Integer, List<String>>();
        for (File f : files) {
            int individualParseId = new Integer(f.getName().split("-")[0]);
            String stationNameWithDotTxt = (f.getName().split("-")[2]);
            String stationName = stationNameWithDotTxt.substring(0, (stationNameWithDotTxt.length() - 4));

            if (!biDirectionalParseFolders.containsKey(individualParseId)) {
                biDirectionalParseFolders.put(individualParseId, new ArrayList<String>());
            }
            List<String> stationNames = biDirectionalParseFolders.get(individualParseId);
            stationNames.add(stationName);
        }

        List<List<String>> parseFiles = new ArrayList<List<String>>();
        for (int i = 0; i < biDirectionalParseFolders.size(); i++) {
            parseFiles.add(biDirectionalParseFolders.get(i));
        }

        for (int i = 0; i < parseFiles.size(); i++) {
            System.out.println("LongevityRecordedTests.run parsing id: " + i);
            TflSiteScraperFromSavedFilesForTesting scraper = new TflSiteScraperFromSavedFilesForTesting(new File(folder), "" + i);
            BranchIterator branchIterator = new BranchIteratorImpl(fixture.getSerializedFileDaoFactory(), scraper);
            SetMultimap<AbstractDirection, Point> map = branchIterator.run(branchName);
            res.add(branchIterator.run(branchName));

            int count = 0;
            for (AbstractDirection dir : map.keySet()) {
                List<Point> pointList = Lists.newArrayList(map.get(dir));
                count += pointList.size();
                System.out.println(dir + " total: " + pointList.size());
                for (Point point : pointList) {
                    System.out.println(dir + ": " + ((DiscoveredTrain) point).getDescription() + "   ('" + ((DiscoveredTrain) point).getFurthestStation().getStationName() + "')");
                }
            }
            //int totalMeasuredTrains = SingletonStatsCollector.getInstance().allStats().get(branchName).iterator().next().getNumberOfTrainsFound();
            // System.out.println("total trains: "+count+ " total measured trains in stats: "+totalMeasuredTrains);

        }

        return res;
    }

    public void testAllInFolder() throws Exception {
        File allRecordingssFolder = new File(htmlsFolder);
        for (File file : allRecordingssFolder.listFiles()) {
            if (file.isDirectory()) {
                runFromDir(file);
            } else if (file.getName().endsWith(".zip")) {
                String folder = System.getProperty("java.io.tmpdir");
                String unzippedFolder = unzip(folder, file.getCanonicalPath());
                runFromDir(new File(folder+unzippedFolder));
                System.out.println("LongevityRecordedTests.testAllInFolder unzippedFolder = " + folder+unzippedFolder);
                FileUtils.deleteDirectory(new File(folder+unzippedFolder));
            }
        }
    }

    private void runFromDir(File folder) throws IOException {
        System.out.println("LongevityRecordedTests.testAllInFolder running longjevity test from data in folder " + folder);
        if (folder.getName().indexOf("_") < 0) return;
        String branchName = folder.getName().substring(0, folder.getName().indexOf("_"));
        run(folder.getCanonicalPath(), branchName);
        printStats(branchName);
    }

    public void xtest_bakerloo__2009_10_31__11_39_10() throws Exception {
        String branchName = "bakerloo";
        String folder = htmlsFolder + branchName + "__2009_10_31__11_39_10";
        List<SetMultimap<AbstractDirection, Point>> results = run(folder, branchName);
        printStats(branchName);
    }

    public void xtest_bakerloo__2009_11_01__19_40_59() throws Exception {
        String branchName = "bakerloo";
        String folder = htmlsFolder + branchName + "__2009_11_01__19_40_59";
        List<SetMultimap<AbstractDirection, Point>> results = run(folder, branchName);
        printStats(branchName);
    }

    public void xtest_piccadilly__2009_11_30__20_34_37() throws Exception {
        String branchName = "piccadilly";
        String folder = htmlsFolder + branchName + "__2009_11_30__20_34_37";
        List<SetMultimap<AbstractDirection, Point>> results = run(folder, branchName);
        printStats(branchName);
    }

    public void xtest_central__2009_11_29__16_47_47() throws Exception {
        String branchName = "central";
        String folder = htmlsFolder + branchName + "__2009_11_29__16_47_47";
        List<SetMultimap<AbstractDirection, Point>> results = run(folder, branchName);
        printStats(branchName);
    }

    private void printStats(String branchName) {
//        Map<String, Deque<SingletonStatsCollector.BranchIterationsStats>> stats = SingletonStatsCollector.getInstance().allStats();
//        Deque<SingletonStatsCollector.BranchIterationsStats> branchIterationsStatsList = stats.get(branchName);
//
//        int count = 0;
//        for (SingletonStatsCollector.BranchIterationsStats iterationsStats : branchIterationsStatsList) {
//            System.out.println("LongevityRecordedTests iter " + ++count + " " + iterationsStats.totalTimeTook() + ", trains: " + iterationsStats.getNumberOfTrainsFound() + ", parses: " + iterationsStats.getStats().size() + ", " + iterationsStats.getCacheHits() + " cache hits done at" + DateFormat.getDateTimeInstance().format( iterationsStats.completionGmtCompletionTime().getTime()));
//        }
    }

    public void xtest_victoria_2009_10_17__17_52_54() throws Exception {
        String branchName = "victoria";
        String folder = htmlsFolder + branchName + "__2009_10_17__17_52_54";
        List<SetMultimap<AbstractDirection, Point>> results = run(folder, branchName);

        AbstractDirection dir = results.get(0).keySet().iterator().next();

        Iterator<Point> points1 = results.get(0).get(dir).iterator();
        Iterator<Point> points2 = results.get(1).get(dir).iterator();

        while (points1.hasNext() && points2.hasNext()) {
            Point point1 = points1.next();
            Point point2 = points2.next();

            System.out.println(point1.getDescription() + " -- " + point2.getDescription());
        }

        printStats(branchName);
    }

    private void assertNoDups(List<Point> points) {
        Set<String> found = new HashSet<String>();

        for (Point point : points) {
            if (found.contains(point)) {
                fail("contains duplicate: " + point);
            } else {
                found.add(point.getDescription());
            }
        }
    }

    public String unzip(String destinationName, String zipFileName) {
        String rootFolder = null;

        try {
            byte[] buf = new byte[1024];
            ZipInputStream zipinputstream;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(new FileInputStream(zipFileName));
            boolean isFirstRun = true;

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = destinationName + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                System.out.println("entryname " + entryName);
                int n;
                File newFile = new File(entryName);
                if (isFirstRun && !zipentry.isDirectory()) {
                    fail("zip file must contain folder at root");
                } else {
                    isFirstRun = false;
                    rootFolder = zipentry.getName();
                }
                if (zipentry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                FileOutputStream fileoutputstream = new FileOutputStream(entryName);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.flush();
                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }//while

            zipinputstream.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return rootFolder;
    }
}
