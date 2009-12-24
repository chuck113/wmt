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

    public FindBranchStopResult getBranchStop(String name, Branch branch){
        Set<BranchStop> branchStopSet = branchStops.get(name);
        if(branchStopSet == null){
            LOG.warn("found no entry for station '"+name+"' on branch '"+(branch==null?"(null)":branch.getName())+"', returning null"); 
            //return null;
            return FindBranchStopResult.unknown();
        }
        if(branchStopSet.size() == 1){
            if(!branch.equals(branchStopSet.iterator().next().getBranch())){
                LOG.warn("returning a stop on a branch which is not the requested branch");
                return FindBranchStopResult.notOnBranch();
            } else{
                return FindBranchStopResult.result(branchStopSet.iterator().next());
            }
        } else{
            if(branch == null){
               LOG.warn("null branch was supplied for getBranchStop, if this is not a test this is an error and may result in incorrect results. returning the firsrt found branch stop..."); 
                return FindBranchStopResult.unknown();
            }
            for (BranchStop stop : branchStopSet) {
                if(stop.getBranch().equals(branch)){
                    // assume no dups on same branch
                    return FindBranchStopResult.result(stop);
                }
            }
            return FindBranchStopResult.notOnBranch();
        }
    }
}
