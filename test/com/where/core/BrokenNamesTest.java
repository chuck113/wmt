package com.where.core;

import junit.framework.TestCase;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.dao.hsqldb.DataMapper;
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



    public void testCanningTownGiveCorrectStation() throws Exception{
        BranchStop stop = factory.getBranchStopDao().getBranchStop("Canning Town", jublieeBranch);
        System.out.println(stop.getTflStationCode());
        assertEquals("CNT", stop.getTflStationCode().getCode());
    }
    
    public void testStJohnsWood() throws Exception{
        {
            String input = "St John's Wood";
            String target= "St. John's Wood";

            parsAndValidate(input, target, jublieeBranch);
        }

        {
            String input = "St Johns Wood";
            String target= "St. John's Wood";

            parsAndValidate(input, target, jublieeBranch);
        }

    }

    public void testWembleyParkSiding() throws Exception{
        String input = "Wembley Park Siding";
        String target= "Wembley Park";

        parsAndValidate(input, target, jublieeBranch);
    }
    
    public void testSouthOfOxfordCircus() throws Exception{
        String input = "South of Oxford Circus";
        String expected = "Oxford Circus";

        parsAndValidate(input, expected, victoriaBranch);
    }

    public void testQueensParkNorthSidings() throws Exception{
        String input = "Queen's Park North Sidings";
        String expected = "Queen's Park";

        parsAndValidate(input, expected, jublieeBranch);
    }

    public void testRegentsPark() throws Exception{
        String input = "Regents Park";
        String expected = "Regent's Park";

        parsAndValidate(input, expected, victoriaBranch);
    }

    public void testNorthOfQueensPark() throws Exception{
        String input = "North of Queen's Park";
        String expected = "Queen's Park";

        parsAndValidate(input, expected, jublieeBranch);
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



    private void parsAndValidate(String input, String expected, Branch branch) {
        List<String> list = BoardParsing.parse(input, null);
        assertTrue(list.size() == 1);
        System.out.println("BrokenNamesTest.parsAndValidate entry is: "+list.get(0));
        assertNotNull(validation.vaidateStation(list.get(0), branch));
        assertEquals(expected, validation.vaidateStation(list.get(0), branch).getStation().getName());
    }

    //TODO
    //Headstone Lane

}
