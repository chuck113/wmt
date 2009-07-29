package com.where.core;

import junit.framework.TestCase;
import com.where.dao.DataMapperImpl;
import com.where.dao.SerializedFileLoader;
import com.where.dao.DataMapper;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.domain.alg.StationValidation;

import java.util.List;

/**
 * @author Charles Kubicek
 */
public class BrokenNamesTest extends TestCase {

    private DataMapper dataMapper;
    private StationValidation validation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        dataMapper = new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME));
        validation = new StationValidation(dataMapper);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }


    //[WARN]  didn't find station for string: 'St John's Wood' (StationValidation.java:69, thread main)
    public void testStJohnsWood() throws Exception{

        String found = "St John's Wood";
        String target= "St. John's Wood";

        Branch branch = dataMapper.getBranchNamesToBranches().get("jubilee");
        List<BranchStop> stops = dataMapper.getBranchStops(branch);

        for (BranchStop stop : stops) {
            System.out.println("stop: "+stop.getStation().getName());
        }

        assertNull(validation.vaidateStation(found));
       assertNotNull(validation.vaidateStation(target));
    }

    //[WARN]  didn't find station for string: 'Wembley Park Siding' (StationValidation.java:69, thread main)
    public void testWembleyParkSiding() throws Exception{
        String found = "Wembley Park Siding";
        String target= "Wembley Park";

       assertNotNull(validation.vaidateStation(found));
        assertEquals(validation.vaidateStation(found).getStation().getName(), target);
    }

    public void testCanningTownGiveCorrectStation() throws Exception{
        BranchStop stop = dataMapper.getBranchStopFromStationName("Canning Town");
        System.out.println(stop.getStationCode().getCode());
        assertEquals("CNT", stop.getStationCode().getCode());
    }

    //
    //Between Northumberland Park Depot and Seven Sisters


}
