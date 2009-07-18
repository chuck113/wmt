package com.where.core;
/**
 * */

import junit.framework.*;
import com.where.domain.alg.Algorithm;
import com.where.domain.alg.BoardParsing;
import com.where.domain.alg.DiscoveredTrain;
import com.where.domain.alg.HtmlStationParser;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Branch;
import com.where.dao.HibernateHsqlLoader;
import com.where.dao.DataMapperImpl;
import com.where.dao.SerializedFileLoader;
import com.where.dao.DataMapper;
import com.where.domain.Position;
import com.where.domain.Direction;

import java.util.List;

/**
 * bank northern
 * 8 high barnet northern
 * 7 charing cross northern
 * 10 test cross northern
 * 11 test northern northern
 * 12 victoria victoria
 * 13 jubilee jubilee
 * 14 bakerloo bakerloo
 * 15 metropolitan metropolitan
 */
public class AlgorithmTest extends TestCase {
    Algorithm algorithm;
    HtmlStationParser htmlStationParser;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        htmlStationParser = new HtmlStationParser();
    }

    public void testRun() throws Exception {
        Branch branch = HibernateHsqlLoader.instance().getBranchNamesToBranches().get("victoria");
        System.out.println("AlgorithmTest.testRun branch: " + branch);
        algorithm = new Algorithm("victoria", new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME)));

        algorithm.run();
    }

    private void testPosition(String position, String stationAt, String furthestPos) {
        //algorithm = new Algorithm("victoria", new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME)));
        //Position pos = algorithm.makePosition(position, stationAt);
        //BranchStop stop = pos.findFurthest();
        DataMapper dataMapper = new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME));
        BoardParsing bp = new BoardParsing(dataMapper);
        DiscoveredTrain train = bp.findPosition(position, stationAt, null);
        assertEquals(furthestPos, train.getFurthestStation().getStation().getName());

        List<String> result = htmlStationParser.parse(position);
        assertEquals(furthestPos, result.get(0));
    }

    //Unknown
    //At Platform
    //Near High Barnet
    //Leaving Euston
    public void testMakePositionBetween() throws Exception {
        // not really sure which it should return, seems like we should be supplying the direction that is used?
        testPosition("Between High Barnet and Totteridge & Whetstone", null, "High Barnet");
    }

    public void testMakePositionAt() throws Exception {
        testPosition("At East Finchley Platform 4", null, "East Finchley");
    }

    public void testMakePositionBy() throws Exception {
        testPosition("By East Finchley", null, "East Finchley");
    }

    public void testMakePositionLeft() throws Exception {
        testPosition("Left East Finchley", null, "East Finchley");
    }

    public void testMakePositionLeaving() throws Exception {
        testPosition("Leaving East Finchley", null, "East Finchley");
    }

    public void testMakePositionApproaching() throws Exception {
        testPosition("Approaching East Finchley", null, "East Finchley");
    }

    //Leaving Waterloo towards Kennington
    public void testMakePositionLeavingTowards() throws Exception {
        testPosition("Leaving Waterloo towards Kennington", null, "Kennington");
    }
}