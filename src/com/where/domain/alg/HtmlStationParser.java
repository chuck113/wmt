package com.where.domain.alg;

import com.where.domain.Direction;
import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Charles Kubicek
 */
public class HtmlStationParser {

    private final Logger LOG = Logger.getLogger(HtmlTrainStates.class);

    /**
     * */
//    public interface Constants {
//        public static final String AT_PLATFORM = "At Platform";
//        public static final String PLATFORM = "Platform";
//        public static final String BETWEEN = "Between";
//        public static final String AND = "and";
//        public static final String AT = "At";
//        public static final String BY = "By";
//        public static final String LEFT = "Left";
//        public static final String DEP0T = "Depot";
//        public static final String LEAVING = "Leaving";
//        public static final String APPROACHING = "Approaching";
//        public static final String TOWARDS = "towards";
//    }

    public static class HtmlStationStrings {
        private final String firstStation;
        private final String secondStation;

        public HtmlStationStrings(String firstStation) {
            this.firstStation = firstStation;
            this.secondStation = null;
        }

        public HtmlStationStrings(String firstStation, String secondStation) {
            this.firstStation = firstStation;
            this.secondStation = secondStation;
        }

        public String getFirstStation() {
            return firstStation;
        }

        public String getSecondStation() {
            return secondStation;
        }
    }

    public List<String> parse(String htmlStations){
        HtmlTrainStates[] htmlTrainStates = HtmlTrainStates.values();

        for(HtmlTrainStates htmlTrainState : htmlTrainStates){
            List<String> states = htmlTrainState.matches(htmlStations);

            if(states.size() > 0){
                return states;
            }
        }

        LOG.warn("Did not know what to do with html position: "+htmlStations);
        return Collections.emptyList();
    }

    public static enum HtmlTrainStates {

        AT_PLATFORM("At Platform"),
        PLATFORM("Platform"),
        BETWEEN_AND("Between", "and"),
        AT("At"),
        BY("By"),
        LEFT("Left"),
        DEP0T("Depot"),
        LEAVING("Leaving"),
        APPROACHING("Approaching"),
        TOWARDS("towards");

        private final String[] stringsToFind;

        HtmlTrainStates(String ... htmlStrings) {
            stringsToFind = htmlStrings;
        }


        public List<String> matches(String htmlPosition) {
            if(stringsToFind.length == 1 && (htmlPosition.indexOf(stringsToFind[0]) > -1)){
                return Collections.singletonList(closeToAStation(stringsToFind[0], htmlPosition));
            } else if(stringsToFind.length == 2 && (htmlPosition.indexOf(stringsToFind[0]) > -1)){
               String[] strings = htmlPosition.substring(stringsToFind[0].length()).split(stringsToFind[1]); // beween, and                
               return Arrays.asList(StringUtils.trimAll(strings));
            } else {
                return Collections.emptyList();
            }
        }


        private String closeToAStation(String descriptor, String info) {
            String startStation = info.substring(descriptor.length(), info.length());
            if (startStation.indexOf(Constants.PLATFORM) > -1) {
                startStation = startStation.substring(0, startStation.indexOf(Constants.PLATFORM));
            }
            return StringUtils.trim(startStation);
        }
    }

//    public DiscoveredTrain findPosition(String html_position, String stationName, Direction direction){
//        String startStation = null;
//        String endStation = null;
//
//        //'Between High Barnet and Totteridge & Whetstone'
//        if (html_position.indexOf(com.where.domain.alg.Constants.BETWEEN) > -1) {
//            String[] strings = html_position.substring(com.where.domain.alg.Constants.BETWEEN.length()).split(com.where.domain.alg.Constants.AND);
//
//            startStation = StringUtils.trim(strings[0]);
//            endStation = StringUtils.trim(strings[1]);
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.AT_PLATFORM) > -1) {
//            startStation = stationName;
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.AT) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(com.where.domain.alg.Constants.AT, html_position);
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.BY) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(com.where.domain.alg.Constants.BY, html_position);
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.LEFT) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(com.where.domain.alg.Constants.LEFT, html_position);
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.LEAVING) > -1 && html_position.indexOf(com.where.domain.alg.Constants.TOWARDS) > -1) {  //At East Finchley Platform 4
//            LOG.warn("leaving towards not implemented properly");
//            startStation = closeToAStation(com.where.domain.alg.Constants.LEAVING, html_position);
//            // not implemented
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.LEAVING) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(com.where.domain.alg.Constants.LEAVING, html_position);
//        } else if (html_position.indexOf(com.where.domain.alg.Constants.APPROACHING) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(com.where.domain.alg.Constants.APPROACHING, html_position);
//        } else if (html_position.length() > 0) { // have seen just the station name, or just the station name and platofrm X
//            if (html_position.indexOf(com.where.domain.alg.Constants.PLATFORM) > -1) {
//                startStation = html_position.split(com.where.domain.alg.Constants.PLATFORM)[0];
//            } else {
//                startStation = html_position;
//            }
//        }
//
//        return buildPoint(startStation, endStation, direction, html_position);
//    }
}
