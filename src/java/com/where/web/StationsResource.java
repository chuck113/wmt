package com.where.web;

import java.util.Collections;
import java.util.List;
import java.util.Hashtable;
import java.util.ListIterator;

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

import com.where.domain.Branch;
import com.where.domain.BranchStop;
import com.where.domain.Station;
import com.where.domain.DaoFactory;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;

/**
 * @author Charles Kubicek
 *
 * Stations are served statically so this isn't actually called during normal app use
 */
public class StationsResource extends WmtResource {

    private final Logger LOG = Logger.getLogger(WmtResource.class);
    private String lineName;
    private final static Hashtable<String, String> CACHED_BRANCHES = new Hashtable<String, String>();
    private DaoFactory daoFactory;

    @Override  
    protected void doInit() throws ResourceException {
        super.doInit();
        this.lineName = getRestPathAttribute(WmtRestApplication.LINE_URL_PATH_NAME);
        daoFactory = new ClasspathFileDataSource().getDaoFactory();
    }

    private String /*List<BranchStop>*/ doGet(String lineName){
        if(StringUtils.isEmpty(lineName)) {
            LOG.warn("Did not find line for string input: "+lineName);
            return "{"+STATIONS+": { "+STATIONS_ARRAY+" : [[]]}}";
        } else {
            if(!CACHED_BRANCHES.containsKey(lineName)){
                LinkedHashMultimap<String,Branch> hashMultimap = daoFactory.getLineDao().getLinesToBranches();
                List<List<BranchStop>> stops = Lists.newArrayList();

                for(Branch branch : hashMultimap.get(lineName)){
                    stops.add(daoFactory.getBranchDao().getBranchStops(branch));
                }
                CACHED_BRANCHES.put(lineName, makeStopsJson(stops));
            }
            return CACHED_BRANCHES.get(lineName);
        }
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Get//("json")
    public String toJson() throws ResourceException {
        return doGet(lineName);
    }

    private String makeStopsJson(List<List<BranchStop>> stops) {
        StringBuffer buf = new StringBuffer("{"+STATIONS+": { "+STATIONS_ARRAY+" : [\n");
        String  spaceColonSpace = " : ";
        String commaSpace = ", ";

        for (List<BranchStop> branch : stops) {
            buf.append("  [\n");
            for (ListIterator<BranchStop> iter = branch.listIterator(); iter.hasNext();) {
                BranchStop stop = iter.next();
                Station station = stop.getStation();
                String stationCode = stop.getTflStationCode().getCode();
                buf.append("    { "+LAT+spaceColonSpace+ station.getLat()+commaSpace+LNG+spaceColonSpace + station.getLng()+ commaSpace+NAME+spaceColonSpace+wrapInQuotes(station.getName())+commaSpace+CODE+spaceColonSpace+wrapInQuotes(stationCode)+" }");
                if(iter.hasNext()){
                    buf.append(",\n");
                }else{
                    buf.append("\n");                    
                }
            }
            buf.append("  ],\n");
        }
        buf.append("]}}");
        return buf.toString();
    }

    private static String wrapInQuotes(String st) {
        return "\"" + st + "\"";
    }

    private final String STATIONS = "\"stations\"";
    private final String STATIONS_ARRAY = "\"stationsArray\"";

    private final String LAT = "\"lat\"";
    private final String LNG = "\"lng\"";
    private final String NAME = "\"name\"";
    private final String CODE = "\"code\"";
}
