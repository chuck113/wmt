package com.where.web;

import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Application;

public class WmtRestApplication extends Application {
    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createRoot() {
        // Create a router Restlet that routes each call to a
        // new instance of HelloWorldResource.
        Router router = new Router(getContext());

        // Defines only one route
        router.attach("/loc/{branch}", BranchResource.class);

        return router;
    }
}
