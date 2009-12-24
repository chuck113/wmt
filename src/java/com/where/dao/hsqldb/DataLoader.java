package com.where.dao.hsqldb;

import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.google.common.collect.LinkedHashMultimap;

import java.util.List;
import java.util.Map;

/**
 * @author Charles Kubicek
 */
public interface DataLoader {

    String BRANCHES_TO_BRANCH_STOPS_SER = "branchesToBranchStops.ser";
    String BRANCH_STOPS_TO_BRANCHES_SER = "branchStopsToBranches.ser";
    String BRANCH_NAMES_TO_BRANCHES_SER = "branchNamesToBranches.ser";
    String STATION_NAMES_TO_BRANCH_STOPS_SER = "stationNamesToBranchStops.ser";
    String LINE_NAMES_TO_BRANCHES_SER = "lineNamesToBranches.ser";

    Map<Branch, List<BranchStop>> getBranchesToBranchStops();

    Map<BranchStop, Branch> getBranchStopsToBranches();

    Map<String, Branch> getBranchNamesToBranches();

    Map<String, BranchStop> getStationNamesToBranchStops();

    LinkedHashMultimap<String, Branch> getLineNamesToBranches();
}
