package com.where.dao.hsqldb;


import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.where.hibernate.Station;
import com.where.hibernate.TflStationCode;

import java.util.Map;
import java.util.List;

/**
 * Ideally this class will never be garbage collected - except for tests
 */
public class DataMapperImpl implements DataMapper{
    //private static final DataLoader LOADER = SerializedFileLoader.instance();
    private final DataLoader dataLoader;

    public DataMapperImpl(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public Map<BranchStop, Branch> getBranchStopsToBranches() {
        return dataLoader.getBranchStopsToBranches();
    }

    public BranchStop getBranchStopFromStationName(String stationName) {
        return dataLoader.getStationNamesToBrancheStops().get(stationName);
    }

    public Map<String, Branch> getBranchNamesToBranches() {
        return dataLoader.getBranchNamesToBranches();
    }

    public List<BranchStop> getBranchStops(Branch branch){
        return dataLoader.getBranchesToBranchStops().get(branch);
    }

//    public TflStationCode getStationsToCodes(Station station){
//        return dataLoader.getStationsToCodes().get(station);
//    }
}
