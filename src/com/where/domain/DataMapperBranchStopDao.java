package com.where.domain;

import com.where.dao.hsqldb.DataMapper;
import com.where.domain.BranchStop;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Charles Kubicek
 */
public class DataMapperBranchStopDao implements BranchStopDao {
    private final Map<String, Set<BranchStop>> branchStops;
    private static final Logger LOG = Logger.getLogger(DataMapperBranchStopDao.class);

    public DataMapperBranchStopDao(Map<String, Set<BranchStop>> branchStops) {
         this.branchStops = branchStops;
    }

    public BranchStop getBranchStop(String name, Branch branch){
        Set<BranchStop> branchStopSet = branchStops.get(name);
        if(branchStopSet == null){
            LOG.warn("found no entry for station '"+name+"' on branch '"+(branch==null?"(null)":branch.getName())+"', returning null"); 
             return null;
        }
        if(branchStopSet.size() == 1){
            return branchStopSet.iterator().next();
        } else{
            if(branch == null){
               LOG.warn("null branch was supplied for getBranchStop, if this is not a test this is an error and may result in incorrect results. returning the firsrt found branch stop..."); 
                return branchStopSet.iterator().next();
            }
            for (BranchStop stop : branchStopSet) {
                if(stop.getBranch().equals(branch)){
                    return stop;
                }
            }
            return null;
        }
    }
}
