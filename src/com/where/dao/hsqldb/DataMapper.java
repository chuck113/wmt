package com.where.dao.hsqldb;

import java.util.Map;
import java.util.List;

public interface DataMapper {
    public Map<com.where.hibernate.BranchStop, com.where.hibernate.Branch> getBranchStopsToBranches();

    public com.where.hibernate.BranchStop getBranchStopFromStationName(String stationName);

    public Map<String, com.where.hibernate.Branch> getBranchNamesToBranches();

    public List<com.where.hibernate.BranchStop> getBranchStops(com.where.hibernate.Branch branch);
}
