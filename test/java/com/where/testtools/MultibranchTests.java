package com.where.testtools;

import junit.framework.TestCase;
import com.where.domain.alg.BranchIterator;
import com.where.domain.alg.LineIteratorImpl;
import com.where.domain.alg.LineIterator;
import com.where.domain.alg.BranchIteratorImpl;
import com.where.core.WhereFixture;
import com.where.tfl.grabber.RecordingTFLSiteScraper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MultibranchTests extends TestCase {

    LineIterator lineIterator;
    String lineName;
    WhereFixture fixture;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        //lineName = "northern";
        //lineName = "central";
        lineName = "piccadilly";
        //branchName = "victoria";
        //branchName = "bakerloo";
        //branchName = "hammersmith";
        fixture = new WhereFixture();

        lineIterator = new LineIteratorImpl(fixture.getSerializedFileDaoFactory(), new RecordingTFLSiteScraper());
    }

    public void testLongevity()throws Exception{
        // "yyyyy.MMMMM.dd GGG hh:mm aaa"    ->>  1996.July.10 AD 12:08 PM
        String id = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss").format(new Date());
        String longevityFolder = "recorded-long/"+lineName+"__"+id;
        System.out.println("IterateBranchTest.testLongevity longevityFolder : '"+longevityFolder+"'");
        int count = 0;
        while(true){
             lineIterator = new LineIteratorImpl(fixture.getSerializedFileDaoFactory(),
                     new RecordingTFLSiteScraper(new RecordingTFLSiteScraper.DataRecordingConfig(""+count++,longevityFolder)));
            lineIterator.run(lineName);
            Thread.sleep(30 * 1000);
        }
    }

    public void testRunOnce() throws Exception {
        lineIterator.run(lineName);
    }
}
