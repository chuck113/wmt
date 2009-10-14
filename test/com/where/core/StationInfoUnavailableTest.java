package com.where.core;

import junit.framework.TestCase;
import com.where.tfl.grabber.TagSoupResultBuilderParser;
import com.where.tfl.grabber.BoardParserResult;

import java.net.URL;

import org.apache.commons.io.IOUtils;

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
        TagSoupResultBuilderParser parser = new TagSoupResultBuilderParser();

        URL resource = StationInfoUnavailableTest.class.getResource("/jubilee-info-unavailable.htm");
        System.out.println("StationInfoUnavailableTest.testParseStationUnavailable resource: " +resource);
        String rawHtml = IOUtils.toString(resource.openStream());
        BoardParserResult result = parser.parse(rawHtml);
        assertTrue(result.getBoardData().isEmpty());
        assertEquals(BoardParserResult.BoardParserResultCode.UNAVAILABLE, result.getResultCode());
    }

    public static void testBrokenKingsCross() throws Exception{
        TagSoupResultBuilderParser parser = new TagSoupResultBuilderParser();

        URL resource = StationInfoUnavailableTest.class.getResource("/kingsCrossFailedParses.html");
        String rawHtml = IOUtils.toString(resource.openStream());
        BoardParserResult result = parser.parse(rawHtml);

        

    }
}
