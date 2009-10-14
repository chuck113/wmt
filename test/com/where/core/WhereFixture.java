package com.where.core;

import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.dao.hsqldb.DataMapper;
import com.where.domain.DaoFactory;
import com.where.domain.DataMapperDaoFactoryImpl;

/**
 * @author Charles Kubicek
 */
public class WhereFixture {

    public DataMapper getSerializedFileDataMapper(){
        return new DataMapperImpl(SerializedFileLoader.Factory.fromClassPath());
    }

    public DaoFactory getSerializedFileDaoFactory(){
        return new DataMapperDaoFactoryImpl(getSerializedFileDataMapper());
    }
}
