package com.where.dao;

import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;

import java.util.List;
import java.util.Map;

/**
 * @author Charles Kubicek
 */
public interface DataLoader {
    Map<Branch, List<BranchStop>> getBranchesToBranchStops();

    Map<BranchStop, Branch> getBranchStopsToBranches();

    Map<String, Branch> getBranchNamesToBranches();

    Map<String, BranchStop> getStationNamesToBrancheStops();
}
