package com.where.core;

import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.dao.hsqldb.DataMapper;
import com.where.domain.DaoFactory;
import com.where.domain.HsqlSerializedFileDaoImpl;

/**
 * @author Charles Kubicek
 */
public class WhereFixture {

    public DataMapper getSerializedFileDataMapper(){
        return new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME));
    }

    public DaoFactory getSerializedFileDaoFactory(){
        return new HsqlSerializedFileDaoImpl(getSerializedFileDataMapper());
    }
}
