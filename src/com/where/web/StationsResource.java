package com.where.web;

import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Station;
import com.where.domain.Point;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * @author Charles Kubicek
 */
public class StationsResource extends WmtResource {

    private final Logger LOG = Logger.getLogger(WmtResource.class);
    private final String lineName;


    public StationsResource(Context context, Request request, Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        this.lineName = getRestPathAttribute(WmtRestApplication.LINE_URL_PATH_NAME);
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if(StringUtils.isBlank(lineName)) {
            LOG.warn("Did not find line for string input: "+lineName);
            return returnAsJson(makeStopsJson(Collections.<BranchStop>emptyList()));
        } if(lineName.equals("all")){
           return returnAsJson(makeStopsJson(Collections.<BranchStop>emptyList()));
        } else {
            List<BranchStop> stops = getDataMapper().getBranchStops(getDataMapper().getBranchNamesToBranches().get(lineName));
            return returnAsJson(makeStopsJson(stops));
        }

    }


    //{"menu": {
//  "id": "file",
//  "value": "File",
//  "popup": {
//    "menuitem": [
//      {"value": "New", "onclick": "CreateNewDoc()"},
//      {"value": "Open", "onclick": "OpenDoc()"},
//      {"value": "Close", "onclick": "CloseDoc()"}
//    ]
//  }

    //}}
    // "points": [{"x" : "xx"}, {"x" : "xx"}]
    private StringBuffer makeStopsJson(List<BranchStop> stops) {
        StringBuffer buf = new StringBuffer("{\"stations\": { \"stationsArray\" : [\n");

        for (BranchStop stop : stops) {
            Station station = stop.getStation();
            buf.append("  { \"lat\" : " + station.getLat() + ", \"lng\" : " + station.getLng() + " , \"name\" : \""+station.getName()+"\"},\n");
        }
        buf.append("]}}");
        return buf;
    }

}
