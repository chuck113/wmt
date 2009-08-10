import java.net.URL;

import com.where.tfl.grabber.TagSoupParser;
import org.apache.commons.io.IOUtils;

/**
 * */
public class TagSoupParserTest {

  public static void main(String[] args) {
    TagSoupParserTest tests = new TagSoupParserTest();
    tests.parse("testHTMLs/simple.html");
  }

  private void parse(String file) {
    try {
      URL url = Thread.currentThread().getContextClassLoader().getResource(file);
      new TagSoupParser().parse(IOUtils.toString(url.openStream()));
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
