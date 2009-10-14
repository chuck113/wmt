package com.where.domain;

import com.where.dao.hsqldb.DataMapper;

/**
 * @author Charles Kubicek
 */
public class DataMapperDaoFactoryImpl implements DaoFactory{

    public DataMapperDaoFactoryImpl(DataMapper mapper) {
        this.branchDao = new DataMapperBranchDao(mapper);
        this.branchStopDao = new DataMapperBranchStopDao(mapper);
    }

    private DataMapperBranchStopDao branchStopDao;
    private DataMapperBranchDao branchDao;

    public BranchDao getBranchDao(){
        return branchDao;
    }

    public BranchStopDao getBranchStopDao(){
        return branchStopDao;
    }
}
