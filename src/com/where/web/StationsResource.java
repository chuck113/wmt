package com.where.web;

import java.util.Collections;
import java.util.List;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.where.hibernate.BranchStop;
import com.where.hibernate.Station;

/**
 * @author Charles Kubicek
 */
public class StationsResource extends WmtResource {

    private final Logger LOG = Logger.getLogger(WmtResource.class);
    private String lineName;
    private final static Hashtable<String, List<BranchStop>> CACHED_BRANCHES = new Hashtable<String, List<BranchStop>>(); 

    @Override  
    protected void doInit() throws ResourceException {
        super.doInit();
        this.lineName = getRestPathAttribute(WmtRestApplication.LINE_URL_PATH_NAME);
    }

    private List<BranchStop> doGet(String lineName){
        if(StringUtils.isEmpty(lineName)) {
            LOG.warn("Did not find line for string input: "+lineName);
            return Collections.<BranchStop>emptyList();
        } else {
            if(CACHED_BRANCHES.containsKey(lineName)){
                return CACHED_BRANCHES.get(lineName);
            }else{
                List<BranchStop> stops = getDataMapper().getBranchStops(getDataMapper().getBranchNamesToBranches().get(lineName));
                CACHED_BRANCHES.put(lineName, stops);
                return stops;
            }
        }
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Get("json")
    public String toJson() throws ResourceException {
        return makeStopsJson(doGet(lineName));
    }

    private String makeStopsJson(List<BranchStop> stops) {
        StringBuffer buf = new StringBuffer("{\"stations\": { \"stationsArray\" : [\n");

        for (BranchStop stop : stops) {
            Station station = stop.getStation();
            String stationCode = stop.getStationCode().getCode();
            buf.append("  { \"lat\" : " + station.getLat() + ", \"lng\" : " + station.getLng() + " , \"name\" : \""+station.getName()+"\", \"code\" : \""+stationCode+"\" },\n");
        }
        buf.append("]}}");
        return buf.toString();
    }

}
