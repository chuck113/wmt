package com.where.domain;

import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.DataMapper;
import com.where.hibernate.*;

/**
 * @author Charles Kubicek
 */
public class AbstractHsqlSerializedFileDao {

    protected final DataMapper mapper;

    public AbstractHsqlSerializedFileDao(DataMapper mapper) {
        this.mapper = mapper;
    }

    public Branch convertBranch(com.where.hibernate.Branch branch) {
        return new Branch(branch.getName(), branch.getLine());
    }

    public Station convertStation(com.where.hibernate.Station station) {
        return new Station(station.getName(), station.getLine(), station.getX(), station.getY());
    }

    public TflStationCode makeCode(com.where.hibernate.BranchStop branchStop) {
        com.where.hibernate.TflStationCode code = branchStop.getStationCode();
        com.where.hibernate.Station station = branchStop.getStation();
        com.where.domain.Station domainStation = convertStation(station);

        return new TflStationCode(domainStation, code.getCode(), code.getLine());
    }
}
