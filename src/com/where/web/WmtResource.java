package com.where.web;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.resource.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;
import com.where.dao.DataMapper;
import com.where.dao.DataMapperImpl;
import com.where.dao.SerializedFileLoader;

import javax.servlet.ServletContext;

/**
 * @author Charles Kubicek
 */
public class WmtResource extends Resource {
    public static final String WEB_INF = "/WEB-INF";
    public static final String SERIALIZED_DATA_FOLDER = "/serailized-tube-data";
    private static final RestCacheSingleton CACHE = RestCacheSingleton.instance();

    private final Logger LOG = Logger.getLogger(WmtResource.class);

    private final String serializedDataFolder;
    private static DataMapper DATA_MAPPER;

    public WmtResource(Context context, Request request, Response response) {
        super(context, request, response);

        serializedDataFolder = getServletContext().getRealPath(WEB_INF + SERIALIZED_DATA_FOLDER);

        if(DATA_MAPPER == null){
            DATA_MAPPER = new DataMapperImpl(new SerializedFileLoader(getSerializedDataFolder()));
        }
    }

    public String getSerializedDataFolder() {
        return serializedDataFolder;
    }

    protected String getRestPathAttribute(String attributeName) {
        return (String) getRequest().getAttributes().get(attributeName);
    }

    protected ServletContext getServletContext() {
        ServletContextAdapter adapter = (ServletContextAdapter) getContext();
        return adapter.getServletContext();
    }

    protected DataMapper getDataMapper(){
       return DATA_MAPPER;
    }

    protected StringRepresentation returnAsJson(java.lang.CharSequence json){
        return new StringRepresentation(json, MediaType.TEXT_PLAIN);
    }

    public static RestCacheSingleton getCache() {
        return CACHE;
    }
}
