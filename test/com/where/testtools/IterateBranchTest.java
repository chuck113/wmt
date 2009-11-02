package com.where.testtools;

import junit.framework.TestCase;
import com.where.domain.alg.BranchIterator;
import com.where.tfl.grabber.RecordingTFLSiteScraper;
import com.where.core.WhereFixture;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author Charles Kubicek
 */
public class IterateBranchTest extends TestCase {
    BranchIterator branchIterator;
    String branchName;
    WhereFixture fixture;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        //branchName = "jubilee";
        //branchName = "victoria";
        branchName = "bakerloo";
        //branchName = "hammersmith";
        fixture = new WhereFixture();
    }

    public void testRunOnce() throws Exception {
        branchIterator = new BranchIterator(branchName, fixture.getSerializedFileDaoFactory(), new RecordingTFLSiteScraper());
        branchIterator.run();
    }

    public void testLongevity()throws Exception{
        // "yyyyy.MMMMM.dd GGG hh:mm aaa"    ->>  1996.July.10 AD 12:08 PM
        String id = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss").format(new Date());
        String longevityFolder = "recorded-long/"+branchName+"__"+id;
        System.out.println("IterateBranchTest.testLongevity longevityFolder : '"+longevityFolder+"'");
        int count = 0;
        while(true){
             branchIterator = new BranchIterator(branchName, fixture.getSerializedFileDaoFactory(),
                     new RecordingTFLSiteScraper(new RecordingTFLSiteScraper.DataRecordingConfig(""+count++,longevityFolder)));
            branchIterator.run();
            Thread.sleep(30 * 1000);
        }
    }

    public void testPerformace(){
        long last = System.currentTimeMillis();
        for(int i=0; i<200; i++){
            branchIterator = new BranchIterator(branchName, fixture.getSerializedFileDaoFactory(), new RecordingTFLSiteScraper());
            branchIterator.run();

            if(i%2 == 0){
                long last20 = System.currentTimeMillis() - last;
                last = System.currentTimeMillis();
                long max = Runtime.getRuntime().maxMemory();
                long total = Runtime.getRuntime().totalMemory();
                long free = Runtime.getRuntime().freeMemory();

                //System.out.println("TagSoupPerformance.testTagSoup iter "+i+" last 20 took: "+last20);
                //System.out.println("TagSoupPerformance.testTagSoup max: "+max);
                //System.out.println("TagSoupPerformance.testTagSoup total: "+total);
                System.out.println("TagSoupPerformance.testTagSoup used: "+(total-free));
                //System.out.println("TagSoupPerformance.testTagSoup iter "+i+" free: "+free);
            }
        }
    }
}