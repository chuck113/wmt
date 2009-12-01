package com.where.web;

import com.where.dao.hsqldb.DataMapper;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import org.apache.log4j.Logger;

public class ClasspathFileDataSource extends DataMapperBuilder {

    private static final Logger LOG = Logger.getLogger(WmtResource.class);
    private static String SERIALIZED_DATA_FOLDER = "serailized-tube-data/";

    private DataMapper dataMapper;
    
    public DataMapper getDataMapper(){
       if(dataMapper == null){
        	LOG.info("creating data mapper");
            dataMapper = new DataMapperImpl(SerializedFileLoader.Factory.fromClassPath(Thread.currentThread().getContextClassLoader(), SERIALIZED_DATA_FOLDER));
       }
        return dataMapper;
    }
}
