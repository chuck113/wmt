package performance;

import com.where.tfl.grabber.TagSoupResultBuilderParser;
import com.where.tfl.grabber.TagSoupStationBoardHtmlParser;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

/**
 * @author Charles Kubicek
 */
public class TagSoupPerformance extends TestCase {

    private String projectPath = "C:\\data\\projects\\wheresmytube";
    private String htmlsFolder = projectPath+"\\test-htmls";

    public void testTagSoup()throws Exception{
        long last = System.currentTimeMillis();
        for(int i=0; i<1000; i++){
            TagSoupStationBoardHtmlParser parser = new TagSoupStationBoardHtmlParser();
            String s = IOUtils.toString(new FileInputStream(htmlsFolder + "\\vauxhal-standard.html"));
            parser.parse(s);

            if(i%100 == 0){
                long last20 = System.currentTimeMillis() - last;
                last = System.currentTimeMillis();
                long max = Runtime.getRuntime().maxMemory();
                long total = Runtime.getRuntime().totalMemory();
                long free = Runtime.getRuntime().freeMemory();

                System.out.println("TagSoupPerformance.testTagSoup last 20 took: "+last20);
                //System.out.println("TagSoupPerformance.testTagSoup max: "+max);
                //System.out.println("TagSoupPerformance.testTagSoup total: "+total);
                System.out.println("TagSoupPerformance.testTagSoup free: "+free);
            }
        }
    }
}
