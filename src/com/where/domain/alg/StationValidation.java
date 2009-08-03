package com.where.domain.alg;

import com.where.domain.BranchStop;
import com.where.dao.hsqldb.DataMapper;
import com.where.domain.DaoFactory;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Charles Kubicek
 */
public class StationValidation {

//    public StationValidation(DataMapper loader) {
//        this.dataMapper = loader;
//    }

    public StationValidation(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;

        ValidationStrategies.add(new SuffixRemovalStrategy(daoFactory));
        ValidationStrategies.add(new AlternativeNameStrategy(daoFactory));
    }

    //private final DataMapper dataMapper;
    private final DaoFactory daoFactory;
    private final List<ValidationStrategy> ValidationStrategies = new ArrayList<ValidationStrategy>();

    private final Logger LOG = Logger.getLogger(StationValidation.class);

//   private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<String, String>();


//    static {
//        ALTERNATIVE_NAMES.put("King's Cross", "King's Cross St. Pancras");
//        ALTERNATIVE_NAMES.put("St John's Wood", "St. John's Wood");
//
//        // not validated but may be wrong
//        ALTERNATIVE_NAMES.put("St. Paul's", "St Paul's");
//        ALTERNATIVE_NAMES.put("St. James's Park", "St James's Park");
//    }
//
//    private String alternateNames(String name) {
//        if (ALTERNATIVE_NAMES.containsKey(name))
//            return ALTERNATIVE_NAMES.get(name);
//
//        return name.replace("&amp;", "&");
//    }

    public BranchStop vaidateStation(String station) {
        if (station == null) return null;

        for (ValidationStrategy validationStrategy : ValidationStrategies) {
            BranchStop stop = validationStrategy.find(station);
            if(stop != null)return stop;
        }

        LOG.warn("didn't find station for string: '" + station + "'");
        return null;

//        //BranchStop stop = dataMapper.getBranchStopFromStationName(station);
//        com.where.domain.BranchStop stop = daoFactory.getBranchStopDao().getBranchStop(station);
//
//        if (stop != null) {
//            return stop;
//        }
//
//        stop = daoFactory.getBranchStopDao().getBranchStop(alternateNames(station));
//
//        if (stop != null) {
//            return stop;
//        }
//
////
////    if (stop == null) {
////      if (station.endsWith("Station")) {
////        stop = dataMapper.getBranchStopFromStationName(station.substring(0, station.length() - "Station".length() - 1));
////      }
////    }
//
//        String[] suffixes = new String[]{"Station", "Siding", "Depot"};
//
//        for (int i = 0; i < suffixes.length && stop == null; i++) {
//            String suffix = suffixes[i];
//            stop = stripSuffix(station, suffix);
//        }
//
//        if (stop == null) {
//            stop = this.daoFactory.getBranchStopDao().getBranchStop(station);
//
//            if (stop == null) {
//                LOG.warn("didn't find station for string: '" + station + "'");
//            }
//        }
//
//        return stop;
    }


    /**
     * Strips known suffixes off stations and gets the equivalien branchStop
     * <p/>
     * Won't find new stations! like:
     * Northumberland Park
     *
     * @param station
     * @param suffix
     * @return
     */
    private BranchStop stripSuffix(String station, String suffix) {
        if (station.endsWith(suffix)) {
            return this.daoFactory.getBranchStopDao().getBranchStop(station.substring(0, station.length() - suffix.length() - 1));
        }

        return null;
    }

    private static interface ValidationStrategy {
        BranchStop find(String stationName);
    }

    private static abstract class ValidationStrategyImpl implements ValidationStrategy{
        private final DaoFactory daoFactory;

        public ValidationStrategyImpl(DaoFactory daoFactory) {
            this.daoFactory = daoFactory;
        }

        public BranchStop testStation(String stationName) {
            return this.daoFactory.getBranchStopDao().getBranchStop(stationName);
        }
    }

    private static class SuffixRemovalStrategy extends ValidationStrategyImpl {

        private final String[] suffixes = new String[]{"Station", "Siding", "Depot"};

        public SuffixRemovalStrategy(DaoFactory daoFactory) {
            super(daoFactory);
        }

        public BranchStop find(String stationName) {
            for (int i = 0; i < suffixes.length; i++) {
                String suffix = suffixes[i];
                if (stationName.endsWith(suffix)) {
                    return testStation(stationName.substring(0, stationName.length() - suffix.length() - 1));
                 }
            }
            return null;
        }
    }

    private static class AlternativeNameStrategy extends ValidationStrategyImpl {
        private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<String, String>();

        static {
            ALTERNATIVE_NAMES.put("King's Cross", "King's Cross St. Pancras");
            ALTERNATIVE_NAMES.put("St John's Wood", "St. John's Wood");

            // not validated but may be wrong
            ALTERNATIVE_NAMES.put("St. Paul's", "St Paul's");
            ALTERNATIVE_NAMES.put("St. James's Park", "St James's Park");
        }

        public AlternativeNameStrategy(DaoFactory daoFactory) {
            super(daoFactory);
        }

        public BranchStop find(String stationName) {
             if (ALTERNATIVE_NAMES.containsKey(stationName))
                return testStation(ALTERNATIVE_NAMES.get(stationName));

            return testStation(stationName.replace("&amp;", "&"));
        }
    }

}
