package com.where.dao.hsqldb;

import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.where.hibernate.Station;
import com.where.hibernate.TflStationCode;

import java.util.List;
import java.util.Map;

/**
 * @author Charles Kubicek
 */
public interface DataLoader {
    Map<Branch, List<BranchStop>> getBranchesToBranchStops();

    Map<BranchStop, Branch> getBranchStopsToBranches();

    Map<String, Branch> getBranchNamesToBranches();

    Map<String, BranchStop> getStationNamesToBranchStops();

    //public Map<Station, TflStationCode> getStationsToCodes();
}
