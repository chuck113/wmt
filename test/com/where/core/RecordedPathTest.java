package com.where.core;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

import com.where.domain.alg.BranchIterator;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.DiscoveredTrain;
import com.where.domain.Point;
import com.where.domain.BranchStop;
import com.where.testtools.TflSiteScraperFromSavedFilesForTesting;

/**
 * @author Charles Kubicek
 */
public class RecordedPathTest extends TestCase {

    private final String htmlsFolder = "C:\\data\\projects\\wheresmytube\\htmls\\";
    private final WhereFixture fixture = new WhereFixture();

    public void testVictoriaHappy(){
        String branchName = "victoria";
        runAndAssertResultSize(branchName, branchName+"-happy", 24);
    }

    public void testFailedToReadNorthumberladPark(){
        String branchName = "victoria";
        runAndAssertResultSize(branchName, branchName+"-missread-northumberland-park", 28);
    }

    public void testJubileeNpe(){
        String branchName = "jubilee";
        runAndAssertResultSize(branchName, branchName+"-npe", 32);
    }

     public void testJubileeHappy(){
        String branchName = "jubilee";
        runAndAssertResultSize(branchName, branchName+"-happy", 40);
    }

    public void testJubileeMissingCannonTown(){
        String branchName = "jubilee";
        runAndAssertResultSize(branchName, branchName+"-MissingCannonTown", 30);
    }

    public void testJubileeUnavailableAtBakerSt(){
        String branchName = "jubilee";
        runAndAssertResultSize(branchName, branchName+"-unavailable-at-baker", 31);
    }
    
    public void testVictoriaPimlicoUnavailable(){
        String branchName = "victoria";
        //run(htmlsFolder+branchName+"-pimlico-unavailable", branchName);
        runAndAssertResultSize(branchName, branchName+"-pimlico-unavailable", 22);
    }

    public void testVictoriaHappy2(){
        String branchName = "victoria";
        //run(htmlsFolder+branchName+"-pimlico-unavailable", branchName);
        runAndAssertResultSize(branchName, branchName+"-happy2", 28);
    }

    public void testVictoriaSevenSistersUnknownLocation() throws Exception{
        String branchName = "victoria";
        //run(htmlsFolder+branchName+"-seven-sisters-unknown-location", branchName);
        runAndAssertResultSize(branchName, branchName+"-seven-sisters-unknown-location", 23);
    }

    public void testQueensParkWrongOrder() throws Exception{
        String branchName = "bakerloo";
        runAndAssertResultSize(branchName, branchName+"-queens-park-wrong-order", 23);
    }

    private void runAndAssertResultSize(String branchName, String htmlFile, int expectedResultSize){
        TflSiteScraperFromSavedFilesForTesting scraper = new TflSiteScraperFromSavedFilesForTesting(new File(htmlsFolder+htmlFile));
        BranchIterator branchIterator = new BranchIterator(branchName, fixture.getSerializedFileDaoFactory(), scraper);

        LinkedHashMap<AbstractDirection, List<Point>> map = branchIterator.run();
 
        for(AbstractDirection dir: map.keySet()){
            List<Point> pointList = map.get(dir);
            for (Point point : pointList) {
                System.out.println(dir+": "+((DiscoveredTrain)point).getDescription());
            }
        }

        int resultCount = 0;
        for(AbstractDirection dir: map.keySet()){
          resultCount+=map.get(dir).size();
          assertNoDups(map.get(dir));
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
