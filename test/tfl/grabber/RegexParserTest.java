package tfl.grabber;

import com.where.tfl.grabber.RegexParser;
import com.where.tfl.grabber.TagSoupResultBuilderParser;
import com.where.tfl.grabber.BoardParserResult;
import com.where.dao.hsqldb.TimeInfo;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * @author Charles Kubicek
 */
public class RegexParserTest extends TestCase {

    private String projectPath = "C:\\data\\projects\\wheresmytube";
    private String htmlsFolder = projectPath+"\\test-htmls";

    RegexParser parser;

    @Override
    protected void setUp() throws Exception {
        parser = new RegexParser();
    }

    private static interface FileAction{
        void action(File file);
    }

    public void forEachTestHtmlFolder(FileAction action){
        File[] folders = new File("C:\\data\\projects\\wheresmytube\\htmls").listFiles();
        for(File folder:folders){
            if(folder.isDirectory()){
               File[] files = folder.listFiles();
                for(File file:files){
                    if(file.isFile()){
                        System.out.println("RegexParserTest.parseFilesInFolder parsing "+file);
                        action.action(file);                    
                    }
                }
            }
        }
    }

    public void parseFilesInFolder(String folder)throws Exception{
//        {
//            File[] files = new File(folder).listFiles();
//            for(File file:files){
//                if(file.isFile()){
//                    System.out.println("RegexParserTest.parseFilesInFolder parsing "+file);
//                    BoardParserResult tagResult = new TagSoupResultBuilderParser().parse(IOUtils.toString(new FileInputStream(file)));
//                    BoardParserResult regexResult = new RegexParser().parse(new FileInputStream(file));
//                    boardParserResultEquals(tagResult, regexResult);
//                }
//            }
//        }


            File[] files = new File(folder).listFiles();
            for(File file:files){
                if(file.isFile()){
                    System.out.println("RegexParserTest.parseFilesInFolder parsing "+file);
                    new TagSoupResultBuilderParser().parse(IOUtils.toString(new FileInputStream(file)));                    
                }
            }
    
    }

     public void testOneBoard()throws Exception{
         String path = htmlsFolder + "\\oneboard.html";
         BoardParserResult regexResult = parser.parse(new FileInputStream(path));
     }

     public void testNoDetails()throws Exception{
         String path = htmlsFolder + "\\nodetails.html";
         BoardParserResult regexResult = parser.parse(new FileInputStream(path));
     }

    public void parseFolderWithRecordingFolders(String folder)throws Exception{
        long start = System.currentTimeMillis();
        File[] files = new File(folder).listFiles();
        for(File file:files){
            if(file.isDirectory()){
               parseFilesInFolder(file.getAbsolutePath());
            }
        }
        System.out.println("time taken for was " + (System.currentTimeMillis() - start));

    }

    public void testParse()throws Exception{
        parseFolderWithRecordingFolders("C:\\data\\projects\\wheresmytube\\htmls");
        //parser.parse(new FileInputStream(htmlsFolder + "\\vauxhal-standard.html"));
       // System.out.println("res: "+res);

    }

    public void testComparision()throws Exception{
        forEachTestHtmlFolder(new FileAction(){
            public void action(File file) {
                try{
                    TagSoupResultBuilderParser tagSoup = new TagSoupResultBuilderParser();
                    BoardParserResult tagResult = tagSoup.parse(IOUtils.toString(new FileInputStream(file)));
                    BoardParserResult regexResult = parser.parse(new FileInputStream(file));
                    boardParserResultEquals(tagResult, regexResult);
                }catch (Exception e){
                    e.printStackTrace();
                    fail(e.getMessage());
                }
            }
        });

    }

    private void boardParserResultEquals(BoardParserResult one, BoardParserResult two){
        if(one.getResultCode().equals(two.getResultCode())){
            Map<String,List<String>> oneMap = one.getBoardData();
            Map<String, List<String>> twoMap = two.getBoardData();

            if(oneMap.entrySet().size() == twoMap.entrySet().size()){
               if(oneMap.keySet().equals(twoMap.keySet())){
                   for(String key : oneMap.keySet()){
                       for(Iterator<String> oneList = oneMap.get(key).iterator(),
                           twoList =twoMap.get(key).iterator(); oneList.hasNext(); ){

                           String oneInfo = oneList.next();
                           String twoInfo = twoList.next();
                           assertEquals(oneInfo, twoInfo);
                       }
                   }
               }else{
                  fail("keys are different"); 
               }
            } else {
                fail("entry sizes are different");
            }
        }
    }
        

    @Override
    protected void tearDown() throws Exception {
    }
}
