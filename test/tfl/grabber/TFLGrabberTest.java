package tfl.grabber;
/**
 * */

import junit.framework.*;
import com.where.tfl.grabber.TFLSiteScraper;
import com.where.tfl.grabber.TrainScraper;
import com.where.tfl.grabber.TagSoupStationBoardHtmlParser;
import com.where.domain.BranchStop;
import com.where.domain.Branch;
import com.where.dao.hsqldb.TimeInfo;
import com.where.core.WhereFixture;

import java.util.List;
import java.util.Map;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;

public class TFLGrabberTest extends TestCase {
  private TrainScraper tflGrabber;
  private WhereFixture fixture;

    private String projectPath = "C:\\data\\projects\\wheresmytube";
    private String htmlsFolder = projectPath+"\\test-htmls";

  protected void setUp() throws Exception {
    super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    tflGrabber = new TFLSiteScraper(TFLSiteScraper.RecordMode.OFF);
    this.fixture = new WhereFixture();
  }

  public void testGet() throws Exception {
      Branch branch = fixture.getSerializedFileDaoFactory().getBranchDao().getBranch("victoria");
    BranchStop stop = fixture.getSerializedFileDaoFactory().getBranchDao().getBranchStops(branch).get(0);

    Map<String,List<TimeInfo>> map = tflGrabber.get(stop,null).getBoardData();

    for (String s : map.keySet()) {
      System.out.println("s: " + s);
      for (TimeInfo get : map.get(s)) {
        System.out.println("info: " + get);
      }
    }
  }

    public void testTagSoupStationBoardHtmlParser() throws Exception{
        TagSoupStationBoardHtmlParser parser = new TagSoupStationBoardHtmlParser();
        String s = IOUtils.toString(new FileInputStream(htmlsFolder + "\\vauxhal-standard.html"));
        String html = parser.parse(s);
        System.out.println("TFLGrabberTest.testTagSoupStationBoardHtmlParser html: "+html);
    }
}