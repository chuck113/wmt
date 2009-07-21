package com.where.core;

import junit.framework.TestCase;

import java.io.File;

import com.where.domain.alg.Algorithm;
import com.where.dao.DataMapperImpl;
import com.where.dao.SerializedFileLoader;
import com.where.tfl.grabber.TFLSiteScraper;

/**
 * @author Charles Kubicek
 */
public class RecordedPathTest extends TestCase {

    public void testVictoriaHappy(){
        String branchName = "victoria";
        File folder = new File("C:\\data\\projects\\wheresmytube\\htmls\\"+branchName+"-happy");
        RecrodedTrainScraper scraper = new RecrodedTrainScraper(folder);
        Algorithm algorithm = new Algorithm(branchName, new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME)), scraper);

        algorithm.run();
    }
}
