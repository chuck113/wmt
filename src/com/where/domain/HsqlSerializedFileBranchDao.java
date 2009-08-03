package com.where.domain;

import com.where.hibernate.*;
//import com.where.hibernate.TflStationCode;
//import com.where.hibernate.Branch;
//import com.where.hibernate.BranchStop;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.DataMapper;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Charles Kubicek
 */
public class HsqlSerializedFileBranchDao extends AbstractHsqlSerializedFileDao implements BranchDao {

    public HsqlSerializedFileBranchDao(DataMapper mapper) {
        super(mapper);
    }

    public Branch getBranch(String name) {
        com.where.hibernate.Branch branch = mapper.getBranchNamesToBranches().get(name);
        return new com.where.domain.Branch(branch.getName(), branch.getLine());
    }

    public List<BranchStop> getBranchStops(Branch branch) {
        com.where.hibernate.Branch mappedBranch = mapper.getBranchNamesToBranches().get(branch.getName());
        List<com.where.hibernate.BranchStop> stops = mapper.getBranchStops(mappedBranch);
        List<BranchStop> result = new ArrayList<BranchStop>(stops.size());

        for (com.where.hibernate.BranchStop branchStop : stops) {
            com.where.hibernate.TflStationCode code = branchStop.getStationCode();
            com.where.hibernate.Station station = branchStop.getStation();
            com.where.domain.Station domainStation = convertStation(station);

            TflStationCode code1 = new TflStationCode(domainStation, code.getCode(), code.getLine());

            result.add(new BranchStop(
                    branchStop.getOrderNo(),
                    branch,
                    code1,
                    domainStation
            ));
        }

        return result;
    }

}
