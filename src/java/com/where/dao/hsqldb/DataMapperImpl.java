package com.where.dao.hsqldb;

import java.util.Map;
import java.util.List;

/**
 * Ideally this class will never be garbage collected - except for tests
 */
public class DataMapperImpl implements DataMapper{
    private final DataLoader dataLoader;

    public DataMapperImpl(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public Map<com.where.hibernate.BranchStop, com.where.hibernate.Branch> getBranchStopsToBranches() {
        return dataLoader.getBranchStopsToBranches();
    }

    public com.where.hibernate.BranchStop getBranchStopFromStationName(String stationName) {
        return dataLoader.getStationNamesToBranchStops().get(stationName);
    }

    public Map<String, com.where.hibernate.Branch> getBranchNamesToBranches() {
        return dataLoader.getBranchNamesToBranches();
    }

    public List<com.where.hibernate.BranchStop> getBranchStops(com.where.hibernate.Branch branch){
        return dataLoader.getBranchesToBranchStops().get(branch);
    }
}
