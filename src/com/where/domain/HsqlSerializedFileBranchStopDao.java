package com.where.domain;

import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.DataMapper;
import com.where.hibernate.*;
import com.where.domain.BranchStop;
import com.where.domain.Branch;

import java.util.Map;

/**
 * @author Charles Kubicek
 */
public class HsqlSerializedFileBranchStopDao extends AbstractHsqlSerializedFileDao implements BranchStopDao {

    public HsqlSerializedFileBranchStopDao(DataMapper mapper) {
        super(mapper);
    }

    public BranchStop getBranchStop(String name){
        com.where.hibernate.BranchStop stop = this.mapper.getBranchStopFromStationName(name);
        if(stop == null) return null;
        com.where.hibernate.Branch branch = this.mapper.getBranchStopsToBranches().get(stop);
        com.where.domain.Branch domainBranch = convertBranch(branch);
        com.where.domain.TflStationCode code = makeCode(stop);
        com.where.domain.Station station = convertStation(stop.getStation());

        return new BranchStop(stop.getOrderNo(), domainBranch, code, station);
    }
}
