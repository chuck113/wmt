package com.where.domain.alg;

import com.where.domain.BranchStop;
import com.where.domain.DaoFactory;
import com.where.domain.Branch;
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

    public BranchStop vaidateStation(String station, Branch branch) {
        if (station == null) return null;

        for (ValidationStrategy validationStrategy : ValidationStrategies) {
            ValidationResult validationResult = validationStrategy.find(station, branch);
            if(validationResult.found){
                return validationResult.result;
            }else if(!validationResult.keepLooking){
                break;
            }
        }

        LOG.warn("didn't find station for string: '" + station + "'");
        return null;
    }

    private final static class ValidationResult{
        BranchStop result;
        boolean found;
        boolean keepLooking;

        public static ValidationResult foundResult(BranchStop result){
            return new ValidationResult(true, false, result);
        }

        public static ValidationResult notFoundButKeepLooking(){
            return new ValidationResult(false, true, null);
        }

        public static ValidationResult notFoundAndQuit(){
            return new ValidationResult(false, false, null);
        }

        private ValidationResult(boolean found, boolean keepLookingAfterFailure, BranchStop result) {
            this.found = found;
            this.keepLooking = keepLookingAfterFailure;
            this.result = result;
        }
    }

    private static interface ValidationStrategy {
        ValidationResult find(String stationName, Branch branch);
    }

    private static abstract class ValidationStrategyImpl implements ValidationStrategy{
        private final DaoFactory daoFactory;

        public ValidationStrategyImpl(DaoFactory daoFactory) {
            this.daoFactory = daoFactory;
        }

        public BranchStop testStation(String stationName, Branch branch) {
            return this.daoFactory.getBranchStopDao().getBranchStop(stationName, branch);
        }

        public ValidationResult testStationAndReturnResult(String stationName, Branch branch) {
            BranchStop res = testStation(stationName, branch);
            if(res == null){
                return ValidationResult.notFoundButKeepLooking();
            } else {
                return ValidationResult.foundResult(res);
            }
        }
    }    

    private static class SuffixRemovalStrategy extends ValidationStrategyImpl {

        private final String[] suffixes = new String[]{"Station", "Depot", "North Sidings", "Siding"};

        public SuffixRemovalStrategy(DaoFactory daoFactory) {
            super(daoFactory);
        }

        public ValidationResult find(String stationName, Branch branch) {
            for (int i = 0; i < suffixes.length; i++) {
                String suffix = suffixes[i];

                // deals with 'Between Queen's Park and North Sidings' where North Sidings
                // isn't actually a station
                if(stationName.length() - suffix.length() <= 0){
                    return ValidationResult.notFoundButKeepLooking();
                }
                if (stationName.endsWith(suffix)) {
                    System.out.println("StationValidation$SuffixRemovalStrategy.find station name: "+stationName+", suffix: "+suffix);
                    return testStationAndReturnResult(stationName.substring(0, stationName.length() - suffix.length() - 1), branch);
                 }
            }
            return ValidationResult.notFoundButKeepLooking();
        }
    }

    private static class AlternativeNameStrategy extends ValidationStrategyImpl {
        private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<String, String>();

        static {
            ALTERNATIVE_NAMES.put("King's Cross", "King's Cross St. Pancras");
            ALTERNATIVE_NAMES.put("St John's Wood", "St. John's Wood");
            ALTERNATIVE_NAMES.put("St Johns Wood", "St. John's Wood");
            ALTERNATIVE_NAMES.put("Regents Park", "Regent's Park");

            // not validated and may be wrong
            ALTERNATIVE_NAMES.put("St. Paul's", "St Paul's");
            ALTERNATIVE_NAMES.put("St. James's Park", "St James's Park");
        }

        public AlternativeNameStrategy(DaoFactory daoFactory) {
            super(daoFactory);
        }

        public ValidationResult find(String stationName, Branch branch) {
             if (ALTERNATIVE_NAMES.containsKey(stationName))
                return testStationAndReturnResult(ALTERNATIVE_NAMES.get(stationName), branch);

            return testStationAndReturnResult(stationName.replace("&amp;", "&"), branch);
        }
    }

}
