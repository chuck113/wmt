package com.where.core;

import junit.framework.TestCase;
import com.where.domain.alg.StationValidation;
import com.where.domain.alg.BoardParsing;
import com.where.domain.Branch;
import com.where.domain.DaoFactory;
import com.where.domain.BranchStop;

import java.util.List;

/**
 * @author Charles Kubicek
 */
public class BrokenNamesTest extends TestCase {

    private DaoFactory factory;
    private StationValidation validation;
    private WhereFixture whereFixture;

    private Branch victoriaBranch;
    private Branch jublieeBranch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        whereFixture = new WhereFixture();
        factory = whereFixture.getSerializedFileDaoFactory();
        validation = new StationValidation(whereFixture.getSerializedFileDaoFactory());

        victoriaBranch = factory.getBranchDao().getBranch("victoria");
        jublieeBranch = factory.getBranchDao().getBranch("jubilee");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void testAtGivenPlatform() throws Exception{
        String input = "At Kilburn Park Platform 2";
        String target= "Kilburn Park";

        parseAndValidate(input, target, jublieeBranch);
    }


    public void testCanningTownGiveCorrectStation() throws Exception{
        BranchStop stop = factory.getBranchStopDao().getBranchStop("Canning Town", jublieeBranch);
        System.out.println(stop.getTflStationCode());
        assertEquals("CNT", stop.getTflStationCode().getCode());
    }
    
    public void testStJohnsWood() throws Exception{
        {
            String input = "St John's Wood";
            String target= "St. John's Wood";

            parseAndValidate(input, target, jublieeBranch);
        }

        {
            String input = "St Johns Wood";
            String target= "St. John's Wood";

            parseAndValidate(input, target, jublieeBranch);
        }

    }

    public void testWembleyParkSiding() throws Exception{
        String input = "Wembley Park Siding";
        String target= "Wembley Park";

        parseAndValidate(input, target, jublieeBranch);
    }
    
    public void testSouthOfOxfordCircus() throws Exception{
        String input = "South of Oxford Circus";
        String expected = "Oxford Circus";

        parseAndValidate(input, expected, victoriaBranch);
    }

    public void testQueensParkNorthSidings() throws Exception{
        String input = "Queen's Park North Sidings";
        String expected = "Queen's Park";

        parseAndValidate(input, expected, jublieeBranch);
    }

    public void testRegentsPark() throws Exception{
        String input = "Regents Park";
        String expected = "Regent's Park";

        parseAndValidate(input, expected, victoriaBranch);
    }

    public void testNorthOfQueensPark() throws Exception{
        String input = "North of Queen's Park";
        String expected = "Queen's Park";

        parseAndValidate(input, expected, jublieeBranch);
    }

    public void testAtPlatform() throws Exception{
        String input = "At Platform";

        // just check it works
        List<String> list = BoardParsing.parse(input, "here");
        assertTrue(list.size() == 1);
        assertEquals(list.iterator().next(), "here");
    }

    public void testNorthumberlandParkDepot() throws Exception{
        String input = "Northumberland Park Depot";
        String expected = "Northumberland Park Depot";

        // just check it works
        List<String> list = BoardParsing.parse(input, null);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0), expected);
    }

    public void testBrokenQueensParkThing() throws Exception{
        String input = "Between Queen's Park and North Sidings";

        // just check it works
        List<String> list = BoardParsing.parse(input, "here");
    }

    public void testBrokenBetween() throws Exception{
        String input = "Highbury & Islington and King's Cross";

        // just check it doesn't throw an exception
        List<String> list = BoardParsing.parse(input, "here");
        //assertTrue(list.size() == 1);
        //assertEquals(list.iterator().next(), "here");
    }

    //Between Queen's Park and North Sidings

    private void parseAndValidate(String input, String expected, Branch branch) {
        List<String> list = BoardParsing.parse(input, null);
        assertTrue(list.size() == 1);
        System.out.println("BrokenNamesTest.parseAndValidate entry is: "+list.get(0));
        assertNotNull(validation.vaidateStation(list.get(0), branch));
        assertEquals(expected, validation.vaidateStation(list.get(0), branch).getStation().getName());
    }

    //TODO
    //Headstone Lane

}
