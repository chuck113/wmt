package com.where.core;

import junit.framework.TestCase;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.dao.hsqldb.DataMapper;
import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.where.domain.alg.StationValidation;
import com.where.domain.alg.BoardParsing;

import java.util.List;

/**
 * @author Charles Kubicek
 */
public class BrokenNamesTest extends TestCase {

    private DataMapper dataMapper;
    private StationValidation validation;
    private WhereFixture whereFixture;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        whereFixture = new WhereFixture();
        dataMapper = whereFixture.getSerializedFileDataMapper();
        validation = new StationValidation(whereFixture.getSerializedFileDaoFactory());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }



    public void testCanningTownGiveCorrectStation() throws Exception{
        BranchStop stop = dataMapper.getBranchStopFromStationName("Canning Town");
        System.out.println(stop.getStationCode().getCode());
        assertEquals("CNT", stop.getStationCode().getCode());
    }
    
    public void testStJohnsWood() throws Exception{
        String input = "St John's Wood";
        String target= "St. John's Wood";

        parsAndValidate(input, target);
    }

    public void testWembleyParkSiding() throws Exception{
        String input = "Wembley Park Siding";
        String target= "Wembley Park";

        parsAndValidate(input, target);
    }
    
    public void testSouthOfOxfordCircus() throws Exception{
        String input = "South of Oxford Circus";
        String expected = "Oxford Circus";

        parsAndValidate(input, expected);
    }

    public void testQueensParkNorthSidings() throws Exception{
        String input = "Queen's Park North Sidings";
        String expected = "Queen's Park";

        parsAndValidate(input, expected);
    }

    public void testRegentsPark() throws Exception{
        String input = "Regents Park";
        String expected = "Regent's Park";

        parsAndValidate(input, expected);
    }

    public void testNorthOfQueensPark() throws Exception{
        String input = "North of Queen's Park";
        String expected = "Queen's Park";

        parsAndValidate(input, expected);
    }

    public void testNorthumberlandParkDepot() throws Exception{
        String input = "Northumberland Park Depot";
        String expected = "Northumberland Park Depot";

        // just check it works
        List<String> list = BoardParsing.parse(input, null);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0), expected);
    }


    private void parsAndValidate(String input, String expected) {
        List<String> list = BoardParsing.parse(input, null);
        assertTrue(list.size() == 1);
        System.out.println("BrokenNamesTest.parsAndValidate entry is: "+list.get(0));
        assertNotNull(validation.vaidateStation(list.get(0)));
        assertEquals(expected, validation.vaidateStation(list.get(0)).getStation().getName());
    }

    //TODO
    //Headstone Lane

}
