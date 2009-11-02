package com.where.core;
/**
 * */

import junit.framework.*;
import com.where.domain.alg.*;
import com.where.domain.Direction;

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
public class MakePositionTest extends TestCase {
    BranchIterator branchIterator;
    BoardParsing boardParsing;
    WhereFixture whereFixture;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        whereFixture = new WhereFixture();
        boardParsing = new BoardParsing(whereFixture.getSerializedFileDaoFactory());
    }

    private void testPosition(String position, String stationAt, String furthestPos) {
        //branchIterator = new BranchIterator("victoria", new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME)));
        //Position pos = branchIterator.makePosition(position, stationAt);
        //BranchStop stop = pos.findFurthest();
        //BoardParsing bp = new BoardParsing(dataMapper);
//        {
//            DiscoveredTrain train = bp.findPosition(position, stationAt, null);
//            assertEquals(furthestPos, train.getFurthestStation().getStation().getName());
//        }
        {
            DiscoveredTrain train1 = boardParsing.findPosition(position, stationAt, Direction.DirectionEnum.NORTHBOUND,null);
            //List<String> result = htmlStationParser.getDepartureBoardElement(position);
            assertEquals(furthestPos, train1.getFurthestStation().getStation().getName());
        }
    }

    //Unknown
    //At Platform
    //Near High Barnet
    //Leaving Euston
    public void testMakePositionBetween() throws Exception {
        // not really sure which it should return, seems like we should be supplying the direction that is used?
        testPosition("Between High Barnet and Totteridge & Whetstone", null, "High Barnet");
    }

    public void testMakePositionAtPlatfrom() throws Exception {
        testPosition("At Platform", "Waterloo", "Waterloo");
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

    public void testMakePositionApproachingSevenSisters() throws Exception {
        testPosition("Approaching Seven Sisters", "Seven Sisters", "Seven Sisters");
    }

    //Leaving Waterloo towards Kennington
    public void testMakePositionLeavingTowards() throws Exception {
        testPosition("Leaving Waterloo towards Kennington", null, "Waterloo");
    }

    //Leaving Waterloo towards Kennington
    public void testMakePositionAtPlatform() throws Exception {
        testPosition("Waterloo Platform 5", null, "Waterloo");
    }

    //didn't find station for string: 'North of Finchley Road' (StationValidation.java:69, thread main)


    //Between Northumberland Park Depot and Seven Sisters
    public void testMakePositionForNorthumberlandPark() throws Exception {
        try{
            testPosition("Between Northumberland Park Depot and Seven Sisters", null, "Seven Sisters");
        }catch(NullPointerException e){
            // station doesn't actually exist!
        }
    }
    //[WARN]  didn't find station for string: 'Northumberl' (StationValidation.java:69, thread main)
}