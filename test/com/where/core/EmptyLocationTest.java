package com.where.core;

import junit.framework.TestCase;
import com.where.domain.Point;
import com.where.domain.alg.Algorithm;
import com.where.dao.DataMapperImpl;
import com.where.dao.SerializedFileLoader;
import com.where.dao.TimeInfo;
import com.where.tfl.grabber.TagSoupParser;
import com.where.tfl.grabber.BoardParserResult;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * @author Charles Kubicek
 */
public class EmptyLocationTest extends TestCase {

    private String projectPath = "C:\\data\\projects\\wheresmytube";
    private String htmlsFolder = projectPath+"\\test-htmls";

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void testFile() throws Exception{
        System.out.println("file is "+htmlsFolder+"\\"+getFileName());
        BoardParserResult result = parse(new File(htmlsFolder + "\\" + getFileName()));

        Map<String,List<TimeInfo>> map = result.getBoardData();
        for(String dir : map.keySet()){
            System.out.println("dir: "+dir);
            for(TimeInfo timeInfos: map.get(dir)){
                System.out.println("  timeInfos: "+timeInfos.getInfo());
            }
        }
    }

    private String getFileName(){
        String className = this.getClass().getSimpleName();
        return className.substring(0, className.length()-4)+".html";
    }

//    public void testVictoriaHappy(){
//        String branchName = "victoria";
//        assertEquals(27, run("C:\\data\\projects\\wheresmytube\\htmls\\"+branchName+"-happy", branchName).size());
//    }
//
//    public void testVictoriaPimlicoUnavailable(){
//        String branchName = "victoria";
//        run("C:\\data\\projects\\wheresmytube\\htmls\\"+branchName+"-pimlico-unavailable", branchName);
//    }

    private BoardParserResult parse(File file) throws Exception{
        TagSoupParser parser = new TagSoupParser();
        return parser.parse(file.toURI().toURL());

        //RecrodedTrainScraper scraper = new RecrodedTrainScraper(new File(file));
       // Algorithm algorithm = new Algorithm(branchName, new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME)), scraper);

       // return algorithm.run();
    }
}
