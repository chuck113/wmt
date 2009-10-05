package com.where.tfl.grabber;

import org.w3c.dom.Node;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.XPathAPI;

import javax.xml.transform.TransformerException;

/**
 * @author Charles Kubicek
 *
 * Returns a chunk of html representing the board
 */
public class TagSoupStationBoardHtmlParser extends TagSoupParser{

    public String parseInternal(Node doc) throws ParseException {
        //final String rootXPath = "/html/body/div/div[2]/div";
        final String rootXPath = "/html/body/div/div[2]";
        return xpath(doc, rootXPath);
    }

    public String parse(String fullHtml) throws ParseException{
        return parseInternal(build(fullHtml));
    }

    protected String xpath(Node doc, String xpath) {
        try {
            XObject res = XPathAPI.eval(doc, buildXPathWithHTMLNamespace(xpath));
            if (res != null) {
                StringBuilder builder = new StringBuilder();
                //System.out.println("TagSoupStationBoardHtmlParser.xpath res: "+res);
                org.w3c.dom.NodeList nodes = res.nodelist();
                //System.out.println("TagSoupStationBoardHtmlParser.xpath   nodes size: "+nodes.getLength());

                for(int i = 0; i<nodes.getLength(); i++) {
                    builder.append(nodes.item(i).toString()+"\n");
                }
                //System.out.println("TagSoupStationBoardHtmlParser.xpath builder: "+builder.toString());

                return builder.toString();
            }

            return null;


        } catch (TransformerException e) {
            return null;
        }
    }
}
