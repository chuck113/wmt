package com.where.tfl.grabber;

import org.w3c.dom.Node;
import org.ccil.cowan.tagsoup.Parser;
import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.apache.commons.io.IOUtils;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.XPathAPI;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.where.dao.hsqldb.TimeInfo;

/**
 * @author Charles Kubicek
 *
 * tag soup:
 * http://www.hackdiary.com/2003/12/28/update-screenscraping-html-with-tagsoup-and-xpath/
 */
public abstract class TagSoupParser {

    private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TagSoupParser.class);


    protected Node build(String rawHtml) throws ParseException {
        try {
            Parser p = new Parser();
            p.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            SAX2DOM sax2dom = new SAX2DOM();
            p.setContentHandler(sax2dom);
            p.parse(new InputSource(IOUtils.toInputStream(rawHtml)));
            Node doc = sax2dom.getDOM();

            return doc;
        } catch (ParserConfigurationException e) {
            throw new ParseException("error parsing raw html", e);
        } catch (IOException e) {
            throw new ParseException("error parsing raw html", e);
        } catch (SAXException e) {
            throw new ParseException("error parsing raw html", e);
        }
    }

    /**
     * Swallows exceptions and returns null
     *
     * @param xpath
     * @return
     */
    protected String xpath(Node doc, String xpath) {
        try {
            XObject res = XPathAPI.eval(doc, buildXPathWithHTMLNamespace(xpath));
            return res == null ? null : res.toString();
        } catch (TransformerException e) {
            LOG.warn("failed to parse xpath: " + xpath + " from doc: " + doc.toString());
            return null;
        }
    }


    protected String buildXPathWithHTMLNamespace(String xpath) {
        return xpath.replaceAll("/", "/html:");
    }
}
