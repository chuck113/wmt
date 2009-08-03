package com.where.domain;

import com.where.dao.hsqldb.DataMapper;
import com.where.dao.hsqldb.DataMapperImpl;

/**
 * @author Charles Kubicek
 */
public class HsqlSerializedFileDaoImpl implements DaoFactory{

    public HsqlSerializedFileDaoImpl(DataMapper mapper) {
        this.branchDao = new HsqlSerializedFileBranchDao(mapper);
        this.branchStopDao = new HsqlSerializedFileBranchStopDao(mapper);
    }

    private HsqlSerializedFileBranchStopDao branchStopDao;
    private HsqlSerializedFileBranchDao branchDao;

    public BranchDao getBranchDao(){
        return branchDao;
    }

    public BranchStopDao getBranchStopDao(){
        return branchStopDao;
    }
}
