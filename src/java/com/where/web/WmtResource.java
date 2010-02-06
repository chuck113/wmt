package com.where.web;

import org.apache.log4j.Logger;
import org.apache.commons.lang.ArrayUtils;
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

import java.util.Date;
import java.util.ArrayList;

/**
 * @author Charles Kubicek
 */
public class WmtResource extends ServerResource {

    @Override  
    protected void doInit() throws ResourceException {
        super.doInit();
    }


    protected String getRestPathAttribute(String attributeName) {
        System.out.println("WmtResource.getRestPathAttribute attss "+ new ArrayList(getRequest().getAttributes().keySet())+" query: "+getQuery().getQueryString());
        return (String) getRequest().getAttributes().get(attributeName);
    }

    protected String getQueryParameter(String param) {
        return getQuery().getFirstValue(param);
    }
}
