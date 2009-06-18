package com.where.dao;

import com.where.dao.pojo.Branch;
import com.where.dao.pojo.BranchStop;

import java.util.Map;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 17-Jun-2009
 * Time: 22:01:29
 * To change this template use File | Settings | File Templates.
 */
public interface NewLoader {

    public Map<BranchStop, Branch> getBranchStopsToBranches();

    public BranchStop getBranchStopFromStationName(String stationName);

    public Map<String, Branch> getBranchNamesToBranches();

    public List<BranchStop> getBranchStops(Branch branch);
}
