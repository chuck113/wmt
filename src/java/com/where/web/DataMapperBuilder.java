package com.where.web;

import com.where.dao.hsqldb.DataMapper;
import com.where.domain.DaoFactory;
import com.where.domain.DataMapperDaoFactoryImpl;

public abstract class DataMapperBuilder {

    abstract DataMapper getDataMapper();

    public DaoFactory getDaoFactory(){
        return new DataMapperDaoFactoryImpl(getDataMapper());
    }
}
