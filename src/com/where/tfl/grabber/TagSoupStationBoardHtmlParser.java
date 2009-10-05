package com.where.tfl.grabber;

import org.w3c.dom.Node;

/**
 * @author Charles Kubicek
 */
public class TagSoupStationBoardHtmlParser extends TagSoupParser{

    public String parseInternal(Node doc) throws ParseException {
        final String rootXPath = "/html/body/div/div[2]/div";
        return xpath(doc, rootXPath);

    }
}
