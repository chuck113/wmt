package com.where.dao;

import com.where.domain.Branch;
import com.where.domain.BranchStop;

import java.util.Map;
import java.util.List;

public interface HibernateLoader {

    public Map<BranchStop, Branch> getBranchStopsToBranches();

    public BranchStop getBranchStopFromStationName(String stationName);

    public Map<String, Branch> getBranchNamesToBranches();

    public List<BranchStop> getBranchStops(Branch branch);
}
