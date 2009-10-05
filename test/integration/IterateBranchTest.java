package integration;

import junit.framework.TestCase;
import com.where.domain.alg.Algorithm;
import com.where.domain.Point;
import com.where.tfl.grabber.CachingTflScraper;
import com.where.tfl.grabber.TFLSiteScraper;
import com.where.core.WhereFixture;

import java.util.Set;

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
        branchName = "victoria";
        //branchName = "bakerloo";
        fixture = new WhereFixture();
    }

    public void testRun() throws Exception {
        //Branch branch = HibernateHsqlLoader.instance().getBranchNamesToBranches().get("victoria");
        //System.out.println("MakePositionTest.testRun branch: " + branch);
        algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), new TFLSiteScraper());

        algorithm.run();
        //assertEquals(0, pointSet.size());
    }
}