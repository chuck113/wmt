package integration;

import junit.framework.TestCase;
import com.where.domain.alg.Algorithm;
import com.where.tfl.grabber.TFLSiteScraper;
import com.where.core.WhereFixture;

/**
 * @author Charles Kubicek
 */
public class IterateBranchTest extends TestCase {
    Algorithm algorithm;
    String branchName;
    WhereFixture fixture;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        //branchName = "jubilee";
        //branchName = "victoria";
        branchName = "bakerloo";
        fixture = new WhereFixture();
    }

    public void testRunOnce() throws Exception {
        algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), new TFLSiteScraper(TFLSiteScraper.RecordMode.ON));
        algorithm.run();
    }

    public void testPerformace(){
        long last = System.currentTimeMillis();
        for(int i=0; i<200; i++){
            algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), new TFLSiteScraper(TFLSiteScraper.RecordMode.OFF));
            algorithm.run();

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