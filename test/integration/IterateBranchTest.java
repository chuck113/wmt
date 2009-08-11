package integration;

import junit.framework.TestCase;
import com.where.domain.alg.Algorithm;
import com.where.domain.alg.HtmlStationParser;
import com.where.domain.Point;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.tfl.grabber.CachingTflScraper;
import com.where.core.WhereFixture;

import java.util.Set;

/**
 * @author Charles Kubicek
 */
public class IterateBranchTest extends TestCase {
    Algorithm algorithm;
    HtmlStationParser htmlStationParser;
    String branchName;
    WhereFixture fixture;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        htmlStationParser = new HtmlStationParser();
        //branchName = "jubilee";
        branchName = "victoria";
        //branchName = "bakerloo";
        fixture = new WhereFixture();
    }

    public void testRun() throws Exception {
        //Branch branch = HibernateHsqlLoader.instance().getBranchNamesToBranches().get("victoria");
        //System.out.println("MakePositionTest.testRun branch: " + branch);
        algorithm = new Algorithm(branchName, fixture.getSerializedFileDaoFactory(), new CachingTflScraper());

        Set<Point> pointSet = algorithm.run();
        //assertEquals(0, pointSet.size());
    }
}