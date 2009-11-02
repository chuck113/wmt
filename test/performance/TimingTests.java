package performance;

import junit.framework.TestCase;
import com.where.core.WhereFixture;
import com.where.core.RecordedPathTest;
import com.where.testtools.TflSiteScraperFromSavedFilesForTesting;
import com.where.domain.alg.BranchIterator;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.DiscoveredTrain;
import com.where.domain.Point;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * @author Charles Kubicek
 */
public class TimingTests extends TestCase {

    private final String htmlsFolder = "C:\\data\\projects\\wheresmytube\\htmls\\";
    private final WhereFixture fixture = new WhereFixture();

    public void testJubileeHappy(){
       String branchName = "jubilee";
        runXtimes(branchName, branchName+"-happy",100);
    }


    private void runXtimes(String branchName, String htmlFile, int iterations){
        List<Long> results = new ArrayList<Long>(iterations);
        TflSiteScraperFromSavedFilesForTesting scraper = new TflSiteScraperFromSavedFilesForTesting(new File(htmlsFolder+htmlFile));
        BranchIterator branchIterator = new BranchIterator(branchName, fixture.getSerializedFileDaoFactory(), scraper);
        for(int i=0; i<iterations; i++){
            long start = new Date().getTime();
            LinkedHashMap<AbstractDirection, List<Point>> map = branchIterator.run();
            long result = new Date().getTime() - start;
            results.add(result);
        }

        long result = 0;
        for(int i=0; i<iterations; i++)result += results.get(i);

        System.out.println("result: "+result +", ave: "+(result/iterations));
    }
}
