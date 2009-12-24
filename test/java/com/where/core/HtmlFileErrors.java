package com.where.core;

import junit.framework.TestCase;
import com.where.tfl.grabber.BoardParserResult;
import com.where.tfl.grabber.TagSoupResultBuilderParser;
import com.where.tfl.grabber.RegexParser;
import com.where.dao.hsqldb.TimeInfo;
import com.where.domain.alg.BoardParsing;
import com.where.core.WhereFixture;

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
    private String htmlsFolder = WhereFixture.TEST_HTMLS_FOLDER;//projectPath+"\\test-htmls";
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

//        Map<String,List<String>> map = result.getBoardData();
//        for(String dir : map.keySet()){
//            System.out.println("dir: "+dir);
//            for(String timeInfos: map.get(dir)){
//                System.out.println("  timeInfos: "+timeInfos);
//            }
//        }

        Map<String, Map<String, List<String>>> boardDataWithPlatformNames = result.getBoardDataWithPlatformNames();
        for (String dir : boardDataWithPlatformNames.keySet()) {
            System.out.println("dir: "+dir);
            Map<String, List<String>> listMap = boardDataWithPlatformNames.get(dir);
            for(String platformnames : listMap.keySet()){
                System.out.println("  plat: '"+platformnames+"'");
                 for(String timeInfos: listMap.get(platformnames)){
                    System.out.println("    timeInfos: "+timeInfos);
                }
             }
        }
    }

    public void testNorthActionOob()throws Exception{
         File f = new File(htmlsFolder + "\\North_Acton-causing-oob.html");
        BoardParserResult result = parse(f);                
    }

    public void testJubileeNpe()throws Exception{
        File f = new File(htmlsFolder + "\\kingsbury-causing-npe.html");
        BoardParserResult result = parse(f);
        Map<String, Map<String, List<String>>> boardDataWithPlatformNames = result.getBoardDataWithPlatformNames();
        Map<String, List<String>> north = boardDataWithPlatformNames.get("Northbound");
        Map<String, List<String>> south = boardDataWithPlatformNames.get("Southbound");
        System.out.println("HtmlFileErrors.testJubileeNpe north: "+north);
        System.out.println("HtmlFileErrors.testJubileeNpe south: "+south);
    }

    public void test_sevenSistersMultipleNorthboundBoards()throws Exception{
        // dir: Northbound
        //  plat: 'Platform 3'
        //    timeInfos: At Platform
        //    timeInfos: At Finsbury Park Platform 2
        //  plat: 'Platform 4'
        //    timeInfos: At Euston Platform 4
        //dir: Southbound
        //  plat: 'Platform 5'
        //    timeInfos: Northumberland Park Depot
        //    timeInfos: At Walthamstow Central Platform 2
        File f = new File(htmlsFolder + "\\sevenSisters-multipleNorthboundBoards.html");
        BoardParserResult result = parse(f);

        Map<String, Map<String, List<String>>> boardDataWithPlatformNames = result.getBoardDataWithPlatformNames();
        Map<String, List<String>> north = boardDataWithPlatformNames.get("Northbound");
        Map<String, List<String>> south = boardDataWithPlatformNames.get("Southbound");

        assertEquals(2, north.size());
        assertEquals(1, south.size());

        List<String> stringList = north.get("Platform 4");
        System.out.println("HtmlFileErrors.test_sevenSistersMultipleNorthboundBoards Platform 4 is "+stringList);

        assertEquals(2, north.get("Platform 3").size());
        assertEquals(1, north.get("Platform 4").size());
        assertEquals(2, south.get("Platform 5").size());
        printResult(result);
    }   

    public void testAtStation()throws Exception{
        File f = new File(htmlsFolder + "\\atStation.html");
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
