package com.where.core;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

import com.where.domain.alg.Algorithm;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.Point;

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
        runAndAssertResultSize(branchName, branchName+"-npe", 30);
    }

    public void testJubileeHappy(){
        String branchName = "jubilee";
        runAndAssertResultSize(branchName, branchName+"-happy", 23);
    }

    public void testJubileeUnavailableAtBakerSt(){
        String branchName = "jubilee";
        runAndAssertResultSize(branchName, branchName+"-unavailable-at-baker", 30);
    }
    
    public void testVictoriaPimlicoUnavailable(){
        String branchName = "victoria";
        //run(htmlsFolder+branchName+"-pimlico-unavailable", branchName);
        runAndAssertResultSize(branchName, branchName+"-pimlico-unavailable", 22);
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
        RecrodedTrainScraperForTesting scraper = new RecrodedTrainScraperForTesting(new File(htmlsFolder+htmlFile));
        Algorithm algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), scraper);

        LinkedHashMap<AbstractDirection, List<Point>> map = algorithm.run();
        int resultCount = 0;
        for(AbstractDirection dir: map.keySet()){
          resultCount+=map.get(dir).size();
          assertNoDups(map.get(dir));

            for(Point p :map.get(dir)){
                System.out.println("RecordedPathTest "+dir+" "+p);
            }
        }
        assertEquals(expectedResultSize, resultCount);

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

    private LinkedHashMap<AbstractDirection,List<Point>> run(String file, String branchName){
        RecrodedTrainScraperForTesting scraper = new RecrodedTrainScraperForTesting(new File(file));
        Algorithm algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), scraper);

        return algorithm.run();
    }
}
