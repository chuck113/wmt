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

    Logger LOG = Logger.getLogger(RegexParser.class);

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
        Map<String, List<TimeInfo>> res = new HashMap<String, List<TimeInfo>>();

        String element = getDepartureBoardElement(in);
        if(!StringUtils.isEmpty(element)){
            try {
                org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
                Document doc = reader.read(new StringReader(element));
                int tableIndex = 1;
                String caption = null;
                String rootXPath = "/board";

                do {
                    String newPath = rootXPath + "/table[" + tableIndex + "]";
                    String captionXPath = newPath + "/caption";

                    caption = xpath(doc, captionXPath);

                    if(!StringUtils.isEmpty(caption)) {
                        caption = caption.substring(0, caption.indexOf(' '));
                        List<TimeInfo> timeInfos = new ArrayList<TimeInfo>();
                        res.put(caption, timeInfos);

                        for (int i = 2; i < 5; i++) {
                            String info =  xpath(doc, newPath + "/tr[" + i + "]/td[2]");
                            String time = xpath(doc, newPath + "/tr[" + i + "]/td[3]");

                            if (info != null) {
                                if ((time != null && time.trim().length() > 0) || (info != null && info.trim().length() > 0)) {
                                    timeInfos.add(new TimeInfo(time.trim(), info.trim()));
                                }
                            }
                        }
                    }
                    tableIndex++;
                } while (caption != null && caption.length() > 0);
            } catch (DocumentException e) {
                LOG.warn("xml parsing exception '"+e.getMessage()+"', ignoring.");
            }
        }
        return resultBuilder(res);
    }

    private BoardParserResult resultBuilder( Map<String, List<TimeInfo>> res) {
        if(res.isEmpty()){
           return new BoardParserResult(BoardParserResult.BoardParserResultCode.UNAVAILABLE, res);
        }else{
           return new BoardParserResult(BoardParserResult.BoardParserResultCode.OK, res);
        }
    }

    public String getDepartureBoardElement(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<p class='timestamp'>")) {
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("<table")) {
                            return grabElement(reader, line);
                        }
                    }

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String grabElement(BufferedReader reader, String lastLine) throws IOException {
        final String terminatingString = "<ul class=\"linklist\">";
        StringBuffer element = new StringBuffer("<board>" + lastLine + "\n");
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.contains(terminatingString))break;
            element.append(line + "\n");
        }

        return element.append("</board>").toString();
    }
}
