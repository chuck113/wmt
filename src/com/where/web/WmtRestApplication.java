package com.where.web;

import org.restlet.Restlet;
import org.restlet.Application;
import org.restlet.routing.Router;

public class WmtRestApplication extends Application {

    public static final String BRANCH_URL_PATH_NAME = "branch";
    public static final String LINE_URL_PATH_NAME = "line";

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        // Defines only one route
        router.attach("/branches/{"+BRANCH_URL_PATH_NAME+"}", BranchesResource.class);
        router.attach("/stations/{"+LINE_URL_PATH_NAME+"}", StationsResource.class);

        return router;
    }
}
