package com.where.domain;

//import com.where.hibernate.TflStationCode;
//import com.where.hibernate.Branch;
//import com.where.hibernate.BranchStop;
import com.where.dao.hsqldb.DataMapper;
import com.where.collect.OrderedMap;

import java.util.*;

/**
 * @author Charles Kubicek
 */
public class DataMapperBranchDao implements BranchDao {

    private final Map<Branch, List<BranchStop>> branchToBranchStopsCache;
    private final Map<String, Branch> branchNamesToBranches;

//    public DataMapperBranchDao(DataMapper mapper) {
//        super(mapper);
//    }

    public DataMapperBranchDao(Map<Branch, List<com.where.domain.BranchStop>> branchToBranchStopsCache, Map<String, Branch> branchNamesToBranches) {
        this.branchToBranchStopsCache = branchToBranchStopsCache;
        this.branchNamesToBranches = branchNamesToBranches;
    }

    public Branch getBranch(String name) {
        return branchNamesToBranches.get(name);
    }

    public List<BranchStop> getBranchStops(Branch branch) {
//        if(!branchToBranchStopsCache.containsKey(branch)){
//            com.where.hibernate.Branch mappedBranch = mapper.getBranchNamesToBranches().get(branch.getName());
//            List<com.where.hibernate.BranchStop> stops = mapper.getBranchStops(mappedBranch);
//            List<BranchStop> result = new ArrayList<BranchStop>(stops.size());
//
//            for (com.where.hibernate.BranchStop branchStop : stops) {
//                com.where.hibernate.TflStationCode code = branchStop.getStationCode();
//                com.where.hibernate.Station station = branchStop.getStation();
//                com.where.domain.Station domainStation = convertStation(station);
//
//                TflStationCode code1 = new TflStationCode(domainStation, code.getCode(), code.getLine());
//
//                result.add(new BranchStop(branchStop.getId(),
//                        branchStop.getOrderNo(),
//                        branch,
//                        code1,
//                        domainStation
//                ));
//            }
//
//            branchToBranchStopsCache.put(branch, result);
//            return result;
//        } else{
            return branchToBranchStopsCache.get(branch);
//        }
    }

    public OrderedMap<BranchStop> getIndexedBranchStops(Branch branch) {
        return new OrderedMap(getBranchStops(branch));
    }
}
