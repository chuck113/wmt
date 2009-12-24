package com.where.domain.alg;

import com.where.domain.BranchStop;
import com.where.domain.DaoFactory;
import com.where.domain.Branch;
import com.where.domain.FindBranchStopResult;
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
        validationStrategies.add(new SuffixRemovalStrategy(daoFactory));
        validationStrategies.add(new AlternativeNameStrategy(daoFactory));
    }

    private final List<ValidationStrategy> validationStrategies = new ArrayList<ValidationStrategy>();

    private final Logger LOG = Logger.getLogger(StationValidation.class);

    public FindBranchStopResult vaidateStation(String station, Branch branch) {
        if (station == null) return null;
        //System.out.println("StationValidation.vaidateStation validating station "+station);

        for (ValidationStrategy validationStrategy : validationStrategies) {
            FindBranchStopResult result = validationStrategy.find(station, branch);
            if(result.hasResult()){
                return result;
            }else if(result.isOnAnotherBranch()){
                break;
            }
        }

        LOG.warn("didn't find station for string: '" + station + "'");
        return FindBranchStopResult.unknown();
    }

//    private final static class ValidationResult{
//        BranchStop result;
//        boolean found;
//        boolean keepLooking;
//
//        static ValidationResult foundResult(BranchStop result){
//            return new ValidationResult(true, false, result);
//        }
//
//        static ValidationResult notFoundButKeepLooking(){
//            return new ValidationResult(false, true, null);
//        }
//
//        static ValidationResult notFoundAndQuit(){
//            return new ValidationResult(false, false, null);
//        }
//
//        static ValidationResult fromResult(){
//            return new ValidationResult(false, false, null);
//        }
//
//        private ValidationResult(boolean found, boolean keepLookingAfterFailure, BranchStop result) {
//            this.found = found;
//            this.keepLooking = keepLookingAfterFailure;
//            this.result = result;
//        }
//    }

    private static interface ValidationStrategy {
        FindBranchStopResult find(String stationName, Branch branch);
    }

    private static abstract class ValidationStrategyImpl implements ValidationStrategy{
        private final DaoFactory daoFactory;

        public ValidationStrategyImpl(DaoFactory daoFactory) {
            this.daoFactory = daoFactory;
        }

        public FindBranchStopResult testStation(String stationName, Branch branch) {
            return this.daoFactory.getBranchStopDao().getBranchStop(stationName, branch);
        }

        public FindBranchStopResult testStationAndReturnResult(String stationName, Branch branch) {
            //FindBranchStopResult res = testStation(stationName, branch);
            return testStation(stationName, branch);
//            if(res.hasResult()){
//                return ValidationResult.foundResult(res.getResult());
//            } else {
//                return ValidationResult.notFoundButKeepLooking();
//            }
        }
    }

    private static class SuffixRemovalStrategy extends ValidationStrategyImpl {

        private final String[] suffixes = new String[]{"Station", "Depot", "North Sidings", "Siding"};

        public SuffixRemovalStrategy(DaoFactory daoFactory) {
            super(daoFactory);
        }

        public FindBranchStopResult find(String stationName, Branch branch) {
            for (int i = 0; i < suffixes.length; i++) {
                String suffix = suffixes[i];

                // deals with 'Between Queen's Park and North Sidings' where North Sidings
                // isn't actually a station
                if(stationName.length() - suffix.length() <= 0){
                    //return ValidationResult.notFoundButKeepLooking();
                    return FindBranchStopResult.unknown();
                }
                if (stationName.endsWith(suffix)) {
                    System.out.println("StationValidation$SuffixRemovalStrategy.find station name: "+stationName+", suffix: "+suffix);
                    return testStationAndReturnResult(stationName.substring(0, stationName.length() - suffix.length() - 1), branch);
                 }
            }
            //return ValidationResult.notFoundButKeepLooking();
            return FindBranchStopResult.unknown();
        }
    }

    private static class AlternativeNameStrategy extends ValidationStrategyImpl {
        private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<String, String>();

        static {
            ALTERNATIVE_NAMES.put("King's Cross", "King's Cross St. Pancras");
            ALTERNATIVE_NAMES.put("St John's Wood", "St. John's Wood");
            ALTERNATIVE_NAMES.put("St Johns Wood", "St. John's Wood");
            ALTERNATIVE_NAMES.put("Regents Park", "Regent's Park");
            ALTERNATIVE_NAMES.put("Central Finchley", "Finchley Central");
            ALTERNATIVE_NAMES.put("Shepherd's Bush", "Shepherd's Bush (Central)");

            // not validated and may be wrong
            ALTERNATIVE_NAMES.put("St Paul's", "St. Paul's");
            ALTERNATIVE_NAMES.put("St James's Park", "St. James's Park");
        }

        public AlternativeNameStrategy(DaoFactory daoFactory) {
            super(daoFactory);
        }

        public FindBranchStopResult find(String stationName, Branch branch) {
            if("Shepherd's Bush".equals(stationName)){
                    System.out.println("StationValidation.vaidateStation");
                }
             if (ALTERNATIVE_NAMES.containsKey(stationName))
                return testStationAndReturnResult(ALTERNATIVE_NAMES.get(stationName), branch);

            return testStationAndReturnResult(stationName.replace("&amp;", "&"), branch);
        }
    }

}
