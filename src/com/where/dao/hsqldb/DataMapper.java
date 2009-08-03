package com.where.dao.hsqldb;

import com.where.hibernate.BranchStop;
import com.where.hibernate.Branch;
import com.where.hibernate.TflStationCode;
import com.where.hibernate.Station;

import java.util.Map;
import java.util.List;

public interface DataMapper {
    public Map<BranchStop, Branch> getBranchStopsToBranches();

    public BranchStop getBranchStopFromStationName(String stationName);

    public Map<String, Branch> getBranchNamesToBranches();

    public List<BranchStop> getBranchStops(Branch branch);

    //public TflStationCode getStationsToCodes(Station station);
}
