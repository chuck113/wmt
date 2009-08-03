package com.where.core;

import junit.framework.TestCase;

import java.io.File;
import java.util.Set;

import com.where.domain.alg.Algorithm;
import com.where.domain.Point;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;

/**
 * @author Charles Kubicek
 */
public class RecordedPathTest extends TestCase {

    private final String htmlsFolder = "C:\\data\\projects\\wheresmytube\\htmls\\";
    private final WhereFixture fixture = new WhereFixture();

    public void testVictoriaHappy(){
        String branchName = "victoria";
        assertEquals(22, run(htmlsFolder+branchName+"-happy", branchName).size());
    }

    public void testVictoriaWrongDirection(){                                
        String branchName = "victoria";
        assertEquals(22, run(htmlsFolder+branchName+"-wrong-direction", branchName).size());
    }

    public void testFailedToReadNorthumberladPark(){
        String branchName = "victoria";
        assertEquals(27, run(htmlsFolder+branchName+"-missread-northumberland-park", branchName).size());
    }

    public void testJubileeNpe(){
        String branchName = "jubilee";
        assertEquals(27, run(htmlsFolder+branchName+"-npe", branchName).size());
    }

    public void testJubileeHappy(){
        String branchName = "jubilee";
        assertEquals(23, run(htmlsFolder+branchName+"-happy", branchName).size());
    }

    //cannon town wasn't working properly as it had an invalid tfl station url code
    public void testJubileeHappyWithCannonTown(){
        String branchName = "jubilee";
        assertEquals(24, run(htmlsFolder+branchName+"-happy-3-with-cannon-town", branchName).size());
    }
    
    public void testVictoriaPimlicoUnavailable(){
        String branchName = "victoria";
        run(htmlsFolder+branchName+"-pimlico-unavailable", branchName);
    }

    public void testVictoriaSevenSistersUnknownLocation() throws Exception{
        String branchName = "victoria";
        run(htmlsFolder+branchName+"-seven-sisters-unknown-location", branchName);
    }

    private Set<Point> run(String file, String branchName){
        RecrodedTrainScraper scraper = new RecrodedTrainScraper(new File(file));
        Algorithm algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), scraper);

        return algorithm.run();
    }
}
