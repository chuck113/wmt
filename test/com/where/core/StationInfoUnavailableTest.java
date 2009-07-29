package com.where.core;

import junit.framework.TestCase;
import com.where.tfl.grabber.TagSoupParser;
import com.where.tfl.grabber.BoardParserResult;

import java.net.URL;

/**
 * @author Charles Kubicek
 */
public class StationInfoUnavailableTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static void testUnavailableHtml() throws Exception{
        TagSoupParser parser = new TagSoupParser();

        URL resource = StationInfoUnavailableTest.class.getResource("/jubilee-info-unavailable.htm");
        System.out.println("StationInfoUnavailableTest.testParseStationUnavailable resource: " +resource);
        BoardParserResult result = parser.parse(resource);
        assertTrue(result.getBoardData().isEmpty());
        assertEquals(TagSoupParser.BoardParserResultCode.UNAVAILABLE, result.getResultCode());
    }
}
