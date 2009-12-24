package com.where.tfl.grabber;

import org.dom4j.DocumentException;
import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import java.io.*;
import java.util.*;

import com.google.common.collect.*;

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

    private static class XpathDocFacade {
        private final org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        private Document document;

        private static final String rootXPath = "/board";

        public XpathDocFacade(String element) {
            try {
                document = reader.read(new StringReader(element));
            } catch (DocumentException e) {
                document = null;
            }
        }

        public String getCaption(int tableIndex) {
            String newPath = rootXPath + "/table[" + tableIndex + "]";
            return xpath(newPath + "/caption");
        }

        public String getDepartureBoardLine(int tableIndex, int tableRowIndex) {
            String newPath = rootXPath + "/table[" + tableIndex + "]";
            String res = xpath(newPath + "/tr[" + tableRowIndex + "]/td[2]");
            if (res != null && res.trim().length() > 0) {
                return res.trim();
            } else {
                return null;
            }
        }

        private String xpath(String xpath) {
            Node node = document.selectSingleNode(xpath);
            if (node != null) {
                return node.getText();
            } else {
                return null;
            }
        }
    }

    private String getDirectionFromCaption(String caption) {
        return caption.substring(0, caption.indexOf(' '));
    }

    private String getPlatformName(String tableCaption, String direction) {
        return tableCaption.substring(direction.length() + 3, tableCaption.length());
    }

    public BoardParserResult parse(InputStream in) {
        Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String, List<String>>>();

        String element = getDepartureBoardElement(in);

        if (!StringUtils.isEmpty(element)) {
            XpathDocFacade doc = new XpathDocFacade(element);
            int tableIndex = 1;
            String tableCaption;

            do {
                tableCaption = doc.getCaption(tableIndex);

                if (!StringUtils.isEmpty(tableCaption)) {
                    String direction = getDirectionFromCaption(tableCaption);//tableCaption.substring(0, tableCaption.indexOf(' '));
                    System.out.println("RegexParser.parse direction is " + direction + ", tableCaption is " + tableCaption);

                    // bug for North Action - doesn't have a direction, instead goes straigh to platform
                    if (isDirectionValid(direction)) {
                        String platform = getPlatformName(tableCaption, direction);//tableCaption.substring(direction.length()+3, tableCaption.length());
                        if (!result.containsKey(direction)) {
                            result.put(direction, new HashMap<String, List<String>>());
                        }

                        result.get(direction).put(platform, findResultsInBoard(
                                doc, tableIndex, collectLists(result.get(direction).values())));
                    }

                }
                tableIndex++;
            } while (tableCaption != null && tableCaption.length() > 0);
            // }
        }
        return resultBuilderNew(result);
    }

    private boolean isDirectionValid(String directionString){
        return !directionString.startsWith("Platform");
    }

    private <T> ImmutableSet<T> collectLists(Collection<List<T>> lists){
        Set<T> result = Sets.newHashSet();
        for(List<T> list : lists) result.addAll(list);
        return ImmutableSet.copyOf(result);
    }

    private List<String> findResultsInBoard(XpathDocFacade doc, int tableIndex, ImmutableSet<String> foundTrainsForDirection) {
        Set<String> foundTrains = Sets.newHashSet(foundTrainsForDirection);
        ArrayList<String> res = new ArrayList<String>();
        for (int tableRowIndex = 2; tableRowIndex < 5; tableRowIndex++) {
            //String info = xpath(doc, newPath + "/tr[" + tableRowIndex + "]/td[2]");
            String info = doc.getDepartureBoardLine(tableIndex, tableRowIndex);
            //String time = xpath(doc, newPath + "/tr[" + tableRowIndex + "]/td[3]");

            if (info != null) {
                // if (time == null) time = "";
                //TimeInfo timeInfo = new TimeInfo(time.trim(), info.trim());
                if (!foundTrains.contains(info)) {
                    foundTrains.add(info);
                    res.add(info);
                }
            }
        }
        return res;
    }

    private BoardParserResult resultBuilderNew(Map<String, Map<String, List<String>>> res) {
        if (res.isEmpty()) {
            return new BoardParserResult(BoardParserResult.BoardParserResultCode.UNAVAILABLE, res);
        } else {
            return new BoardParserResult(BoardParserResult.BoardParserResultCode.OK, res);
        }
    }

    /**
     * @return the HTML table element that contains the boards with train arrivals,
     *         Returns null if the Html element wasn't found
     */
    public String getDepartureBoardElement(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String res = null;
            String line;

            reader.skip(5000);
            //int byteCounter = 0;
            outer:
            while ((line = reader.readLine()) != null) {
                //byteCounter += line.getBytes().length;
                if (line.contains("<p class='timestamp'>")) {
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("<table")) {
                            res = grabElement(reader, line);
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
            LOG.warn("while parsing url input stream", e);
            return null;
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
