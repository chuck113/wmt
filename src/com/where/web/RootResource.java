package com.where.web;

import org.restlet.resource.ResourceException;
import org.restlet.resource.Get;
import com.where.domain.Point;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author Charles Kubicek
 */
public class RootResource extends WmtResource {

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Get("json")
    public String toJson() {
        return makeJsonLineLinks(WmtProperties.LINES_TO_ITERATE);
    }

     private String makeJsonLineLinks(Set<String> lines) {
        StringBuffer buf = new StringBuffer("{\"lines\": { \"linesArray\" : [\n");

        for (String line : lines) {
            buf.append("  { \"name\" : \"" + line + "\", \"ref\" : \"/"+WmtRestApplication.LINE_RESOURCE_NAME+"/" + line + "\"},\n");
        }
        buf.append("]}}");
        return buf.toString();
    }
}
