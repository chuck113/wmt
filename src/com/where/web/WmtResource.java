package com.where.web;

import org.apache.log4j.Logger;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
//import org.restlet.resource.StringRepresentation;
//import org.restlet.data.Request;
//import org.restlet.data.Response;
import com.where.dao.hsqldb.DataMapper;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.domain.DaoFactory;
import com.where.domain.DataMapperDaoFactoryImpl;

/**
 * @author Charles Kubicek
 */
public class WmtResource extends ServerResource {

    public static final String SERIALIZED_DATA_FOLDER = "serailized-tube-data/";
    private static final Logger LOG = Logger.getLogger(WmtResource.class);

    private static DataMapper DATA_MAPPER;

    @Override  
    protected void doInit() throws ResourceException {
        if(DATA_MAPPER == null){
        	LOG.info("creating data mapper");
            DATA_MAPPER = new DataMapperImpl(SerializedFileLoader.Factory.fromClassPath(Thread.currentThread().getContextClassLoader(), SERIALIZED_DATA_FOLDER));
        }
    }


    protected String getRestPathAttribute(String attributeName) {
        return (String) getRequest().getAttributes().get(attributeName);
    }

    protected DataMapper getDataMapper(){
       return DATA_MAPPER;
    }

    protected DaoFactory getDaoFactory(){
        return new DataMapperDaoFactoryImpl(DATA_MAPPER);
    }
}
