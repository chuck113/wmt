package com.where.core;

import junit.framework.TestCase;
import com.where.tfl.grabber.BoardParserResult;
import com.where.tfl.grabber.TagSoupResultBuilderParser;
import com.where.tfl.grabber.RegexParser;
import com.where.dao.hsqldb.TimeInfo;
import com.where.domain.alg.BoardParsing;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * @author Charles Kubicek
 */
public class HtmlFileErrors extends TestCase {

     private String projectPath = "C:\\data\\projects\\wheresmytube";
    private String htmlsFolder = projectPath+"\\test-htmls";
    private BoardParsing boardParsing;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void setUp() throws Exception {
        boardParsing = new BoardParsing(new WhereFixture().getSerializedFileDaoFactory());
    }

    public void printResult(BoardParserResult result) throws Exception{
        //System.out.println("file is "+htmlsFolder+"\\"+getFileName());
        //BoardParserResult result = parse(new File(htmlsFolder + "\\" + getFileName()));

        Map<String,List<TimeInfo>> map = result.getBoardData();
        for(String dir : map.keySet()){
            System.out.println("dir: "+dir);
            for(TimeInfo timeInfos: map.get(dir)){
                System.out.println("  timeInfos: "+timeInfos.getInfo());
            }
        }
    }

    public void testAtStation()throws Exception{
        File f = new File(htmlsFolder + "\\atStation.html");
        System.out.println("HtmlFileErrors.testAtStation parsing "+f);
        BoardParserResult result = parse(f);
        printResult(result);

        
    }

    private String getFileName(){
        String className = this.getClass().getSimpleName();
        return className.substring(0, className.length()-4)+".html";
    }


    private BoardParserResult parse(File file) throws Exception{
        RegexParser parser = new RegexParser();
        return parser.parse(new FileInputStream(file));
    }
}
