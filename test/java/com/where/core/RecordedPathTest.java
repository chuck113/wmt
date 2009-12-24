package com.where.core;

import junit.framework.TestCase;

import java.io.File;
import java.util.*;

import com.where.domain.alg.*;
import com.where.domain.Point;
import com.where.testtools.TflSiteScraperFromSavedFilesForTesting;
import com.where.stats.SingletonStatsCollector;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Lists;

/**
 * @author Charles Kubicek
 */
public class RecordedPathTest extends TestCase {

    private final String htmlsFolder = WhereFixture.HTMLS_FOLDER+"\\";// "C:\\data\\projects\\wheresmytube\\htmls\\";
    private final WhereFixture fixture = new WhereFixture();
    private static final String NORTHERN = "northern";
    private static final String BAKERLOO = "bakerloo";
    private static final String VICTORIA = "victoria";
    private static final String JUBILEE = "jubilee";
    private static final String CENTRAL = "central";
    private static final String PICCADILLY = "piccadilly";

    /**
     * This is the only real test we have, do not change the number. Here are the results
     *
     *  DIR1 (total 12)
     *  Stockwell 3
        1. Brixton	Between Vauxhall and Stockwell	2 mins
        2. Brixton	At Vauxhall Platform 2	4 mins
        3. Brixton Sidings	At Pimlico Platform 2	4 mins

        Pimlico 2
        1. Brixton Sidings	At Platform
        2. Brixton	At Victoria Platform 4	2 mins
        3. Brixton	At Green Park Platform 4	4 mins

        Green Park 2
        1. Brixton	At Platform
        2. Victoria	At Warren Street Platform 4	4 mins
        3. Brixton	Between King's Cross and Euston	7 mins

        King's Cross 3
        1. Brixton	Between Highbury & Islington and King's Cross	1 min
        2. Victoria	 At Finsbury Park Platform 4	6 mins
        3. Brixton	Between Seven Sisters and Finsbury Park	9 mins

        Seven Sisters 2
        1. Unknown	Approaching Seven Sisters	1 min
        2. Unknown	At Walthamstow Central Platform 2	9 mins
        3. Brixton	At Walthamstow Central Platform 1	12 mins

        DIR 2 (total 12)
        Blackhorse Road 3
        1. Walthamstow Central	At Platform
        2. Walthamstow Central	Between Tottenham Hale and Blackhorse Road	1 min
        3. Walthamstow Central	Between Highbury & Islington and Finsbury Park	8 mins

        Highbury & Islington 3
        1. Walthamstow Central	At Platform
        2. Walthamstow Central	Between King's Cross and Highbury & Islington	1 min
        3. Walthamstow Central	Between Euston and King's Cross	4 mins

        Euston 3
        1. Walthamstow Central	Between Warren Street and Euston	1 min
        2. Walthamstow Central	At Oxford Circus Platform 6	4 mins
        3. Northumberland Park Depot	Between Victoria and Green Park	7 mins

        Victoria 3
        1. Victoria	Between Vauxhall and Pimlico	3 mins
        2. Victoria	At Stockwell Platform 1	6 mins
        3. Walthamstow Central	At Brixton Platform 1	10 mins

        Total = 24
     */
    public void testVictoriaHappy(){
        String branchName = VICTORIA;
        runAndAssertResultSize(branchName, branchName+"-happy", /* never change this*/24);
    }

     public void testJubileeHappy(){
        String branchName = JUBILEE;
        runAndAssertResultSize(branchName, branchName+"-happy", 40);
    }

    public void testFailedToReadNorthumberladPark(){
        String branchName = VICTORIA;
        runAndAssertResultSize(branchName, branchName+"-missread-northumberland-park", 28);
    }

    public void testJubileeNpe2(){
        String branchName = JUBILEE;
        runAndAssertResultSize(branchName, branchName+"-npe2", 3);
    }

    public void testSuspectEndingWhileClosed(){
        String branchName = JUBILEE;
        runAndAssertResultSize(branchName, branchName+"-suspect-endingWhileClosed", 5);
    }

    public void testJubileeNpe(){
        String branchName = JUBILEE;
        runAndAssertResultSize(branchName, branchName+"-npe", 30);
    }

    public void testJubileeMissingCannonTown(){
        String branchName = JUBILEE;
        runAndAssertResultSize(branchName, branchName+"-MissingCannonTown", 31);
    }

    public void testVictoriaHappy2(){
        String branchName = VICTORIA;
        runAndAssertResultSize(branchName, branchName+"-happy2", 28);
    }

    public void testVictoriaSevenSistersUnknownLocation() throws Exception{
        String branchName = VICTORIA;
        runAndAssertResultSize(branchName, branchName+"-seven-sisters-unknown-location", 23);
    }

    public void testQueensParkWrongOrder() throws Exception{
        String branchName = BAKERLOO;
        runAndAssertResultSize(branchName, branchName+"-queens-park-wrong-order", 20);
    }

   public void testBakerlooNpe() throws Exception{
        String branchName = BAKERLOO;
        runAndAssertResultSize(branchName, branchName+"-npe", 20);
    }

    public void testBakerlooQueensParkAndNorthSidingsErrorDoesNotThrowNpe() throws Exception{       
        String branchName = BAKERLOO;
        runAndAssertResultSize(branchName, branchName+"-QueensParkAndNorthSidingsError", 9);
    }

    public void testNorthernHappy() throws Exception{
        String branchName = NORTHERN;
        runAndAssertResultSize(branchName, branchName+"-happy", 74);
    }

    public void testNorthernHappy2() throws Exception{
        String branchName = NORTHERN;
        runAndAssertResultSize(branchName, branchName+"-happy2", 53);
    }

    public void testNorthernHappy3() throws Exception{
        String branchName = NORTHERN;
        runAndAssertResultSize(branchName, branchName+"-happy3", 50);
    }

    public void testCentralHappy() throws Exception{
        String branchName = CENTRAL;
        runAndAssertResultSize(branchName, branchName+"-happy", 30);
    }

    public void testCentralHappy2() throws Exception{
        String branchName = CENTRAL;
        runAndAssertResultSize(branchName, branchName+"-happy2", 33);
    }

    public void testCentralHappy3() throws Exception{
        String branchName = CENTRAL;
        runAndAssertResultSize(branchName, branchName+"-happy3", 34);
    }

    public void testCentralHappyIoob() throws Exception{
        String branchName = CENTRAL;
        runAndAssertResultSize(branchName, branchName+"-ioob", 5);
    }

    public void testPiccadillyNsee() throws Exception{
        String branchName = PICCADILLY;
        runAndAssertResultSize(branchName, branchName+"-nsee", 28);
    }

    private void runAndAssertResultSize(String branchName, String htmlFile, int expectedResultSize){
        TflSiteScraperFromSavedFilesForTesting scraper = new TflSiteScraperFromSavedFilesForTesting(new File(htmlsFolder+htmlFile));
        LineIterator branchIterator = new LineIteratorImpl(fixture.getSerializedFileDaoFactory(), scraper);
        long start = new Date().getTime();
        SetMultimap<AbstractDirection,Point> multimap = branchIterator.run(branchName);
        System.out.println("RecordedPathTest.runAndAssertResultSize took "+(new Date().getTime() - start)+" ms");

        int totalTrains = 0;
        for(AbstractDirection dir: multimap.keySet()){
            List<Point> pointList = Lists.newArrayList(multimap.get(dir));
            totalTrains+=pointList.size();
            System.out.println(dir+" total: "+pointList.size());
            for (Point point : pointList) {
                System.out.println(dir+": "+point.getDescription()+"   ('"+((DiscoveredTrain)point).getFurthestStation().getStationName()+"')");
            }
        }

        //int totalMeasuredTrains = SingletonStatsCollector.getInstance().allStats().get(branchName).iterator().next().getNumberOfTrainsFound();
        //System.out.println("total trains: "+totalTrains+ " total measured trains in stats: "+totalMeasuredTrains);

        int resultCount = 0;
        for(AbstractDirection dir: multimap.keySet()){
          resultCount+=multimap.get(dir).size();
          assertNoDups(Lists.newArrayList(multimap.get(dir)));
        }
        assertEquals(expectedResultSize, resultCount);

    }

    private void assertNoDups(List<Point> points){
        Set<Point> found = new HashSet<Point>();

        for (Point point : points) {
             if(found.contains(point)){
                fail("contains duplicate: "+point);
            } else {
                found.add(point);
            }
        }
    }
}
