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

    public StationValidation(DaoFactory daoFactory) {
        ValidationStrategies.add(new SuffixRemovalStrategy(daoFactory));
        ValidationStrategies.add(new AlternativeNameStrategy(daoFactory));
    }

    private final List<ValidationStrategy> ValidationStrategies = new ArrayList<ValidationStrategy>();

    private final Logger LOG = Logger.getLogger(StationValidation.class);

    public BranchStop vaidateStation(String station) {
        if (station == null) return null;

        for (ValidationStrategy validationStrategy : ValidationStrategies) {
            BranchStop stop = validationStrategy.find(station);
            if(stop != null)return stop;
        }

        LOG.warn("didn't find station for string: '" + station + "'");
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

        private final String[] suffixes = new String[]{"Station", "Depot", "North Sidings", "Siding"};

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
            ALTERNATIVE_NAMES.put("Regents Park", "Regent's Park");

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
