package com.where.web;

import org.restlet.Restlet;
import org.restlet.Application;
import org.restlet.routing.Router;

public class WmtRestApplication extends Application {

    public static final String BRANCH_URL_PATH_NAME = "branch";
    public static final String LINE_URL_PATH_NAME = "line";

    public static final String BRANCH_RESOURCE_NAME = "branches";
    public static final String STATIONS_RESOURCE_NAME = "stations";    

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        // Defines only one route
        router.attach("/", RootResource.class);
        router.attach("/"+BRANCH_RESOURCE_NAME+"/{"+BRANCH_URL_PATH_NAME+"}", BranchesResource.class);
        router.attach("/"+STATIONS_RESOURCE_NAME+"/{"+LINE_URL_PATH_NAME+"}", StationsResource.class);

        return router;
    }
}
