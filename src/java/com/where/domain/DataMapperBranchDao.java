package com.where.domain;

//import com.where.hibernate.TflStationCode;
//import com.where.hibernate.Branch;
//import com.where.hibernate.BranchStop;

import com.where.dao.hsqldb.DataMapper;
import com.where.collect.OrderedMap;
import com.google.common.collect.*;
import com.google.common.base.Function;

import java.util.*;

/**
 * @author Charles Kubicek
 */
public class DataMapperBranchDao implements BranchDao {

    private final Map<Branch, List<BranchStop>> branchToBranchStopsCache;
    private final Map<String, Branch> branchNamesToBranches;
    private final Map<Branch, OrderedMap<BranchStop>> indexedBranchStops;
    private final SetMultimap<Branch, String> branchesToStationNames;

    public DataMapperBranchDao(Map<Branch, List<com.where.domain.BranchStop>> branchToBranchStopsCache,
                               Map<String, Branch> branchNamesToBranches) {
        this.branchToBranchStopsCache = branchToBranchStopsCache;
        this.branchNamesToBranches = branchNamesToBranches;
        this.indexedBranchStops = makeOrderedMapOfBranchStops(branchToBranchStopsCache);
        this.branchesToStationNames = makeBranchesToStationNames(branchToBranchStopsCache);
    }

    private SetMultimap<Branch, String> makeBranchesToStationNames(Map<Branch, List<BranchStop>> branchToBranchStops){
        SetMultimap<Branch, String> map = HashMultimap.create();
        Function f = new Function<BranchStop, String>(){
            public String apply(BranchStop branchStop) {
                return branchStop.getStationName();
            }
        };
        for(Map.Entry<Branch,List<BranchStop>> entry :branchToBranchStops.entrySet()){
            map.putAll(entry.getKey(), Collections2.transform(entry.getValue(), f));
        }
        return map;
    }

    private Map<Branch, OrderedMap<BranchStop>> makeOrderedMapOfBranchStops(
            Map<Branch, List<com.where.domain.BranchStop>> branchToBranchStops){

        Map<Branch, OrderedMap<BranchStop>> res = new HashMap<Branch, OrderedMap<BranchStop>>();

        for (Map.Entry<Branch, List<BranchStop>> entry : branchToBranchStops.entrySet()) {
            res.put(entry.getKey(), new OrderedMap<BranchStop>(entry.getValue()));
        }
        return res;
    }

    public Branch getBranch(String name) {
        return branchNamesToBranches.get(name);
    }

    public List<BranchStop> getBranchStops(Branch branch) {
        return branchToBranchStopsCache.get(branch);
    }

    public OrderedMap<BranchStop> getIndexedBranchStops(Branch branch) {
        return new OrderedMap(getBranchStops(branch));
    }

    public Set<String> getStationNames(Branch branch){
        return branchesToStationNames.get(branch);
    }
}
