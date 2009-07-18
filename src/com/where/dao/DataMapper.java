package com.where.dao;

import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.Station;

import java.util.Map;
import java.util.List;

public interface DataMapper {
    public Map<BranchStop, Branch> getBranchStopsToBranches();

    public BranchStop getBranchStopFromStationName(String stationName);

    public Map<String, Branch> getBranchNamesToBranches();

    public List<BranchStop> getBranchStops(Branch branch);
}
