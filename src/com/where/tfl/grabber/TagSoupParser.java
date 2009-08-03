package com.where.tfl.grabber;

import java.net.URL;

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

import com.where.dao.hsqldb.TimeInfo;

/**
 * */
public class TagSoupParser {
    private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TagSoupParser.class);

    public TagSoupParser() {

    }


    public static enum BoardParserResultCode {
        OK, UNAVAILABLE, PARSE_EXCEPTION;
    }


    private Node build(URL url) throws ParseException {
        try {
            Parser p = new Parser();
            p.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            SAX2DOM sax2dom = new SAX2DOM();
            p.setContentHandler(sax2dom);
            p.parse(new InputSource(url.openStream()));
            Node doc = sax2dom.getDOM();

            return doc;
        } catch (ParserConfigurationException e) {
            throw new ParseException("error parsing url: " + url, e);
        } catch (IOException e) {
            throw new ParseException("error parsing url: " + url, e);
        } catch (SAXException e) {
            throw new ParseException("error parsing url: " + url, e);
        }
    }

    /**
     * Swallows exceptions and returns null
     *
     * @param xpath
     * @return
     */
    private String xpath(Node doc, String xpath) {
        try {
            XObject res = XPathAPI.eval(doc, buildXPathWithHTMLNamespace(xpath));
            if (res != null) {
                return res.toString();
            }

            return null;
        } catch (TransformerException e) {
            LOG.warn("failed to parse xpath: " + xpath + " from doc: " + doc.toString());
            return null;
        }
    }


    public BoardParserResult parse(URL url) throws ParseException {
        LOG.info("getting url: "+url);
        final String rootXPath = "/html/body/div/div[2]/div";
        int tableIndex = 1;
        String caption = null;
        Map<String, List<TimeInfo>> res = new HashMap<String, List<TimeInfo>>();
        Node doc = build(url);
        //System.out.println("url = " + url);
        do {
            String newPath = rootXPath + "/table[" + tableIndex + "]";
            String captionXPath = newPath + "/caption";

            caption = xpath(doc, captionXPath);

            if (caption != null && caption.length() > 0) {
                caption = caption.substring(0, caption.indexOf(' '));
                //System.out.println("TagSoupParser.parse caption: '" + caption + "'");
                List<TimeInfo> timeInfos = new ArrayList<TimeInfo>();
                res.put(caption, timeInfos);

                //String all = xpath(newPath);
                //System.out.println("all: " + all);

                for (int i = 2; i < 5; i++) {
                    String info = xpath(doc, newPath + "/tr[" + i + "]/td[2]");
                    String time = xpath(doc, newPath + "/tr[" + i + "]/td[3]");

                    if (info != null) {

                        //System.out.println("time = " + time.trim());
                        //System.out.println("info = " + info.trim());
                        // write if either the info or the time is not empty
                        if ((time != null && time.trim().length() > 0) || (info != null && info.trim().length() > 0)) {
                            //System.out.println("  time = " + time.trim());
                            //System.out.println("  info = " + info.trim());
                            timeInfos.add(new TimeInfo(time.trim(), info.trim()));
                        }
                    }
                }
            }

            tableIndex++;
        } while (caption != null && caption.length() > 0);

        return resultBuilder(res);
    }

    private BoardParserResult resultBuilder( Map<String, List<TimeInfo>> res) {
        if(res.isEmpty()){
           return new BoardParserResult(BoardParserResultCode.UNAVAILABLE, res);
        }else{
           return new BoardParserResult(BoardParserResultCode.OK, res);
        }
    }

    private String buildXPathWithHTMLNamespace(String xpath) {
        return xpath.replaceAll("/", "/html:");
    }
}
