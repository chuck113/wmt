package com.where.web;

import org.restlet.Restlet;
import org.restlet.Application;
import org.restlet.routing.Router;

public class WmtRestApplication extends Application {

    public static final String LINE_URL_PATH_NAME = "line";
    public static final String LINE_RESOURCE_NAME = "lines";
    public static final String STATIONS_RESOURCE_NAME = "stations";
    

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
       Router router = new Router(getContext());

        // Defines only one route
        router.attach("/", RootResource.class);
        router.attach("/"+LINE_RESOURCE_NAME+"/{"+LINE_URL_PATH_NAME+"}", LinesResource.class);
        router.attach("/"+STATIONS_RESOURCE_NAME+"/{"+LINE_URL_PATH_NAME+"}", StationsResource.class);

        return router;
    }
}
