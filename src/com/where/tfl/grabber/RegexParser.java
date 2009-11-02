package com.where.tfl.grabber;

import org.dom4j.DocumentException;
import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

import com.where.dao.hsqldb.TimeInfo;

/**
 * Grab the element using regex and apply xpath to the restul
 */
public class RegexParser {

    private final Logger LOG = Logger.getLogger(RegexParser.class);
    private final org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();


    /**
     * Executes an xpath an returns the element text form *one* node
     *
     * @return
     */
    private String xpath(Document document, String xpath) {
        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            return node.getText();
        } else {
            return null;
        }
    }

    public BoardParserResult parse(InputStream in) {
        Map<String, List<String>> res = new HashMap<String, List<String>>();
        Map<String, Map<String,List<String>>> resNew = new HashMap<String, Map<String,List<String>>>();

        String element = getDepartureBoardElement(in);
        if (!StringUtils.isEmpty(element)) {
            try {
                Document doc = reader.read(new StringReader(element));
                int tableIndex = 1;
                String caption;
                String rootXPath = "/board";

                do {
                    String newPath = rootXPath + "/table[" + tableIndex + "]";
                    String captionXPath = newPath + "/caption";

                    caption = xpath(doc, captionXPath);

                    if (!StringUtils.isEmpty(caption)) {
                        String direction = caption.substring(0, caption.indexOf(' '));
                        String platform = caption.substring(direction.length()+3, caption.length());
                        if(!res.containsKey(direction)){
                            res.put(direction, new ArrayList<String>());
                        }
                        List<String> timeInfos = res.get(direction);


                        if(!resNew.containsKey(direction)){
                            resNew.put(direction, new HashMap<String, List<String>>());
                        }
                        Map<String, List<String>> timeInfosNew = resNew.get(direction);
                        ArrayList platformList = new ArrayList<String>();
                        timeInfosNew.put(platform, platformList);

                        for (int i = 2; i < 5; i++) {
                            String info = xpath(doc, newPath + "/tr[" + i + "]/td[2]");
                            //String time = xpath(doc, newPath + "/tr[" + i + "]/td[3]");

                            if (info != null && info.trim().length() > 0) {
                               // if (time == null) time = "";
                                //TimeInfo timeInfo = new TimeInfo(time.trim(), info.trim());
                                if(!timeInfos.contains(info.trim())){
                                    timeInfos.add(info.trim());
                                    platformList.add(info.trim());
                                }
                            }
                        }
                    }
                    tableIndex++;
                } while (caption != null && caption.length() > 0);
            } catch (DocumentException e) {
                LOG.warn("xml parsing exception '" + e.getMessage() + "', ignoring.");
            }
        }
        return resultBuilderNew(resNew);
    }

        private BoardParserResult resultBuilderNew(Map<String, Map<String, List<String>>> res) {
        if (res.isEmpty()) {
            return new BoardParserResult(BoardParserResult.BoardParserResultCode.UNAVAILABLE, res);
        } else {
            return new BoardParserResult(BoardParserResult.BoardParserResultCode.OK, res);
        }
    }

//    private BoardParserResult resultBuilder(Map<String, List<String>> res) {
//        if (res.isEmpty()) {
//            return new BoardParserResult(BoardParserResult.BoardParserResultCode.UNAVAILABLE, res);
//        } else {
//            return new BoardParserResult(BoardParserResult.BoardParserResultCode.OK, res);
//        }
//    }

    public String getDepartureBoardElement(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String res = null;
            String line = null;
            outer:while ((line = reader.readLine()) != null) {
                if (line.contains("<p class='timestamp'>")) {
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("<table")) {
                            res= grabElement(reader, line);
                            break outer;
                        }
                    }

                    break;
                }
            }
            reader.close();
            in.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // TODO something better
        }
    }

    private String grabElement(BufferedReader reader, String lastLine) throws IOException {
        final String terminatingString = "<ul class=\"linklist\">";
        StringBuffer element = new StringBuffer("<board>" + lastLine + "\n");
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(terminatingString)) break;
            element.append(line + "\n");
        }

        return element.append("</board>").toString();
    }
}
