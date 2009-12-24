package com.where.tfl.grabber;

import org.w3c.dom.Node;


import java.util.*;

import com.where.dao.hsqldb.TimeInfo;

/**
 * */
public class TagSoupResultBuilderParser extends TagSoupParser{
    private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TagSoupResultBuilderParser.class);

    /**
     * @deprecated - favor @{link RegexParser}
     * @param rawHtml
     * @return
     * @throws ParseException
     */
    public BoardParserResult parse(String rawHtml) throws ParseException {
        return parseInternal(build(rawHtml));
    }

     public BoardParserResult parseInternal(Node doc) throws ParseException {
        final String rootXPath = "/html/body/div/div[2]/div";
        int tableIndex = 1;
        String caption = null;
        Map<String, List<String>> res = new HashMap<String, List<String>>();
        //System.out.println("url = " + url);
        do {
            String newPath = rootXPath + "/table[" + tableIndex + "]";
            String captionXPath = newPath + "/caption";

            caption = xpath(doc, captionXPath);

            if (caption != null && caption.length() > 0) {
                caption = caption.substring(0, caption.indexOf(' '));
                //System.out.println("TagSoupResultBuilderParser.parse caption: '" + caption + "'");

                // bug here about not seeing muliple boards, see regex parser
                List<String> boardTrainInfos = new ArrayList<String>();
                res.put(caption, boardTrainInfos);

                //String all = xpath(newPath);
                //System.out.println("all: " + all);

                for (int i = 2; i < 5; i++) {
                    String info = xpath(doc, newPath + "/tr[" + i + "]/td[2]");
                    //String time = xpath(doc, newPath + "/tr[" + i + "]/td[3]");

                    if (info != null) {

                        //System.out.println("time = " + time.trim());
                        //System.out.println("info = " + info.trim());
                        // write if either the info or the time is not empty
                        if (/*(time != null && time.trim().length() > 0) || */(info != null && info.trim().length() > 0)) {
                            //System.out.println("  time = " + time.trim());
                            //System.out.println("  info = " + info.trim());
                            boardTrainInfos.add(info.trim());
                        }
                    }
                }
            }

            tableIndex++;
        } while (caption != null && caption.length() > 0);

        return resultBuilder(res);
    }

    private Map<String, Map<String, List<String>>> convert(Map<String, List<String>> input){
        Map<String, Map<String, List<String>>> res = new HashMap<String, Map<String, List<String>>>();

        for (String key : input.keySet()) {
            List<String> list = input.get(key);
            res.put(key, Collections.singletonMap("Platform 1", list));
        }

        return res;
    }

    private BoardParserResult resultBuilder( Map<String, List<String>> res) {


        if(res.isEmpty()){
           return new BoardParserResult(BoardParserResult.BoardParserResultCode.UNAVAILABLE, convert(res));
        }else{
           return new BoardParserResult(BoardParserResult.BoardParserResultCode.OK, convert(res));
        }
    }
}
