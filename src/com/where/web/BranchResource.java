package com.where.web;

import org.restlet.resource.*;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;

public class BranchResource extends Resource {

    public BranchResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        
        // This representation has only one type of representation.
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = new StringRepresentation(
                "{\"obj\" : \"val\"}", MediaType.APPLICATION_JSON);
        return representation;
    }
}