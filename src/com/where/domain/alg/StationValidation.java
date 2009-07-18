package com.where.domain.alg;

import com.where.dao.hibernate.BranchStop;
import com.where.dao.DataMapper;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Charles Kubicek
 */
public class StationValidation {

    public StationValidation(DataMapper loader) {
        this.dataMapper = loader;
    }

    private final DataMapper dataMapper;

    private final Logger LOG = Logger.getLogger(StationValidation.class);

    private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<String, String>();


    static {
        ALTERNATIVE_NAMES.put("King's Cross", "King's Cross St. Pancras");
    }

    private String alternateNames(String name) {
        if (ALTERNATIVE_NAMES.containsKey(name))
            return ALTERNATIVE_NAMES.get(name);

        return name.replace("&amp;", "&");
    }

    public BranchStop vaidateStation(String station) {
        if (station == null) return null;

        BranchStop stop = dataMapper.getBranchStopFromStationName(station);

        if (stop != null) {
            return stop;
        }
//
//    if (stop == null) {
//      if (station.endsWith("Station")) {
//        stop = dataMapper.getBranchStopFromStationName(station.substring(0, station.length() - "Station".length() - 1));
//      }
//    }

        String[] suffixes = new String[]{"Station", "Siding", "Depot"};

        for (int i = 0; i < suffixes.length && stop == null; i++) {
            String suffix = suffixes[i];
            stop = stripSuffix(station, suffix);
        }

        if (stop == null) {
            stop = dataMapper.getBranchStopFromStationName(alternateNames(station));

            if (stop == null) {
                LOG.warn("didn't find station for string: '" + station + "'");
            }
        }

        return stop;
    }


    /**
     * Strips known suffixes off stations and gets the equivalien branchStop
     *
     * @param station
     * @param suffix
     * @return
     */
    private BranchStop stripSuffix(String station, String suffix) {
        if (station.endsWith("Station")) {
            return dataMapper.getBranchStopFromStationName(station.substring(0, station.length() - suffix.length() - 1));
        }

        return null;
    }
}
