package com.where.domain;

import com.where.dao.hsqldb.DataMapper;
import com.where.domain.BranchStop;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class DataMapperBranchStopDao extends AbstractDataMapperDao implements BranchStopDao {

    private static final Logger LOG = Logger.getLogger(DataMapperBranchStopDao.class);

    public DataMapperBranchStopDao(DataMapper mapper) {
        super(mapper);
    }

    public BranchStop getBranchStop(String name){
        com.where.hibernate.BranchStop stop = this.mapper.getBranchStopFromStationName(name);
        if(stop == null) {LOG.warn("didn't find branch stop for stop "+name);return null;}
        com.where.hibernate.Branch branch = this.mapper.getBranchStopsToBranches().get(stop);
        com.where.domain.Branch domainBranch = convertBranch(branch);
        com.where.domain.TflStationCode code = makeCode(stop);
        com.where.domain.Station station = convertStation(stop.getStation());

        return new BranchStop(stop.getOrderNo(), domainBranch, code, station);
    }
}
