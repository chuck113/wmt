package com.where.domain;

import com.where.dao.hsqldb.DataMapper;
import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;

import java.util.*;
import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class DataMapperDaoFactoryImpl implements DaoFactory, Serializable {

    private static final Logger LOG = Logger.getLogger(DataMapperDaoFactoryImpl.class);

    private final Map<Branch, List<BranchStop>> branchToBranchStopsCache = new HashMap<Branch, List<BranchStop>>();
    private final Map<String, com.where.domain.Branch> branchNamesToBranches = new HashMap<String, com.where.domain.Branch>();
    private final Map<String, Set<BranchStop>> branchStops = new HashMap<String, Set<BranchStop>>();
    private final LinkedHashMultimap<String, Branch> linesToBranches = LinkedHashMultimap.create();

    public DataMapperDaoFactoryImpl(DataMapper mapper) {
        build(mapper);
        this.lineDao = new DataMapperLineDao(linesToBranches, branchToBranchStopsCache);
        this.branchDao = new DataMapperBranchDao(branchToBranchStopsCache, branchNamesToBranches);
        this.branchStopDao = new DataMapperBranchStopDao(branchStops);
    }

    private DataMapperBranchStopDao branchStopDao;
    private DataMapperBranchDao branchDao;
    private DataMapperLineDao lineDao;

    public DataMapperLineDao getLineDao() {
        return lineDao;
    }

    public BranchDao getBranchDao() {
        return branchDao;
    }

    public BranchStopDao getBranchStopDao() {
        return branchStopDao;
    }

    public void build(DataMapper mapper) {
        for (com.where.hibernate.Branch branch : mapper.getBranchNamesToBranches().values()) {
            com.where.hibernate.Branch mappedBranch = mapper.getBranchNamesToBranches().get(branch.getName());
            List<com.where.hibernate.BranchStop> stops = mapper.getBranchStops(mappedBranch);
            List<BranchStop> result = new ArrayList<BranchStop>(stops.size());

            com.where.domain.Branch domainBranch = convertBranch(branch);
            branchNamesToBranches.put(domainBranch.getName(), domainBranch);
            linesToBranches.put(domainBranch.getLine(), domainBranch);

            for (com.where.hibernate.BranchStop branchStop : stops) {
                com.where.hibernate.TflStationCode code = branchStop.getStationCode();
                com.where.hibernate.Station station = branchStop.getStation();
                com.where.domain.Station domainStation = convertStation(station);

                TflStationCode domainCode = new TflStationCode(domainStation, code.getCode(), code.getLine());
                BranchStop domainBranchStop = new BranchStop(branchStop.getId(),branchStop.getOrderNo(), domainBranch, domainCode, domainStation);

                if(!branchStops.containsKey(station.getName())){
                    branchStops.put(station.getName(), new HashSet<BranchStop>());
                }
                branchStops.get(station.getName()).add(domainBranchStop);
                LOG.debug("Adding branch stop "+domainBranchStop);

                result.add(domainBranchStop);
            }

            branchToBranchStopsCache.put(domainBranch, result);
        }
    }

    public com.where.domain.Branch convertBranch(com.where.hibernate.Branch branch) {
        return new com.where.domain.Branch(branch.getId(), branch.getName(), branch.getLine());
    }

    public com.where.domain.Station convertStation(com.where.hibernate.Station station) {
        return new com.where.domain.Station(station.getId(), station.getName(), station.getLine(), station.getX(), station.getY());
    }

    public TflStationCode makeCode(com.where.hibernate.BranchStop branchStop) {
        com.where.hibernate.TflStationCode code = branchStop.getStationCode();
        com.where.hibernate.Station station = branchStop.getStation();
        com.where.domain.Station domainStation = convertStation(station);

        return new TflStationCode(domainStation, code.getCode(), code.getLine());
    }
}
