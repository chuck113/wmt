package com.where.domain.alg;

import com.where.domain.alg.Constants;
import com.where.domain.alg.StringUtils;
import com.where.domain.*;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * @author Charles Kubicek
 */
public class BoardParsing {

    private static final Logger LOG = Logger.getLogger(BoardParsing.class);

    private final StationValidation stationValidation;

    public BoardParsing(DaoFactory daoFactory) {
        stationValidation = new StationValidation(daoFactory);
    }

    /**
     * A lot of info strings are very similar, i.e:
     * "At East Finchley Platform 4"
     * "By East Finchley Platform 4"
     * "Left East Finchley
     *
     * @return
     */
//    private String closeToAStation(String descriptor, String info) {
//        String startStation = info.substring(descriptor.length(), info.length());
//        if (startStation.indexOf(Constants.PLATFORM) > -1) {
//            startStation = startStation.substring(0, startStation.indexOf(Constants.PLATFORM));
//        }
//        return StringUtils.trim(startStation);
//    }
    public DiscoveredTrain findPosition(String html_position, String stationName, Direction direction, Branch branch) {
        List<String> stringList = parse(html_position, stationName);
        String firstStation = stringList.get(0);

        BranchStop first = stationValidation.vaidateStation(firstStation, branch);

        if (first == null) {
            LOG.warn("could not validate station with name '" + firstStation + "', returning null");
            return null;
        }

        if (stringList.size() == 1) {
            return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction, html_position), first, false);
        } else {
            String secondStation = stringList.get(1);
            BranchStop second = stationValidation.vaidateStation(secondStation, branch);

            if (second == null) {
                LOG.warn("could not validate station with name '" + secondStation + "', returning first station only");
                return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction, html_position), first, false);
            }

            return new DiscoveredTrain(new Point(
                    (second.getStation().getY()) - ((second.getStation().getY() - first.getStation().getY()) / 2),
                    (first.getStation().getX()) + ((second.getStation().getX() - first.getStation().getX()) / 2),
                    direction, html_position), first, true);
        }
    }

//    public DiscoveredTrain findPosition(String html_position, String stationName, Direction direction){
//        String startStation = null;
//        String endStation = null;
//
//        //'Between High Barnet and Totteridge & Whetstone'
//        if (html_position.indexOf(Constants.BETWEEN) > -1) {
//            String[] strings = html_position.substring(Constants.BETWEEN.length()).split(Constants.AND);
//
//            startStation = StringUtils.trim(strings[0]);
//            endStation = StringUtils.trim(strings[1]);
//        } else if (html_position.indexOf(Constants.AT_PLATFORM) > -1) {
//            startStation = stationName;
//        } else if (html_position.indexOf(Constants.AT) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(Constants.AT, html_position);
//        } else if (html_position.indexOf(Constants.BY) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(Constants.BY, html_position);
//        } else if (html_position.indexOf(Constants.LEFT) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(Constants.LEFT, html_position);
//        } else if (html_position.indexOf(Constants.LEAVING) > -1 && html_position.indexOf(Constants.TOWARDS) > -1) {  //At East Finchley Platform 4
//            LOG.warn("leaving towards not implemented properly");
//            startStation = closeToAStation(Constants.LEAVING, html_position);
//            // not implemented
//        } else if (html_position.indexOf(Constants.LEAVING) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(Constants.LEAVING, html_position);
//        } else if (html_position.indexOf(Constants.APPROACHING) > -1) {  //At East Finchley Platform 4
//            startStation = closeToAStation(Constants.APPROACHING, html_position);
//        } else if (html_position.length() > 0) { // have seen just the station name, or just the station name and platofrm X
//            if (html_position.indexOf(Constants.PLATFORM) > -1) {
//                startStation = html_position.split(Constants.PLATFORM)[0];
//            } else {
//                startStation = html_position;
//            }
//        }
//
//        return buildPoint(startStation, endStation, direction, html_position);
//    }

    /**
     * if there are two stations then the firsrt is the furthest away.
     * TODO this seems pretty wiered and we should work this out with the direction and alist of stops.
     *
     * @return
     */
//    public DiscoveredTrain buildPoint(String firstStation, String secondStation, Direction direction, Branch branch, String description) {
//        BranchStop first = stationValidation.vaidateStation(firstStation, branch);
//
//        if (first == null) {
//            LOG.warn("could not validate station with name '" + firstStation + "', returning null");
//            return null;
//        }
//
//        if (secondStation == null) {
//            return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction, description), first, isInbetweenStations);
//        } else {
//            BranchStop second = stationValidation.vaidateStation(secondStation, branch);
//
//            if (second == null) {
//                LOG.warn("could not validate station with name '" + secondStation + "', returning first station only");
//                return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction, description), first, isInbetweenStations);
//            }
//
//            return new DiscoveredTrain(new Point(
//                    (second.getStation().getY()) - ((second.getStation().getY() - first.getStation().getY()) / 2),
//                    (first.getStation().getX()) + ((second.getStation().getX() - first.getStation().getX()) / 2),
//                    direction, description), first, isInbetweenStations);
//        }
//    }


    public static List<String> parse(String htmlStations, String stationAt) {
        HtmlTrainStates[] htmlTrainStates = HtmlTrainStates.values();

        for (HtmlTrainStates htmlTrainState : htmlTrainStates) {
            List<String> states = htmlTrainState.matches(htmlStations, stationAt);

            if (states.size() > 0) {
                return states;
            }
        }

        LOG.info("Did not know what to do with html position: " + htmlStations + " returning anyway as could be a valid station");
        return Collections.singletonList(htmlStations);
    }

    public static enum HtmlTrainStates {
        /**
         * The order of these is important, eg 'At East Finchley Platfrom 4'
         * could be seen just as 'Platform'
         */
        AT_PLATFORM(true, "At Platform"),
        AT("At"),
        PLATFORM("Platform"),
        BETWEEN_AND("Between", " and"), // 2nd string has to have spaces or will match things like 'Northumberland' ('and' at end)
       // AND(" and"),  // for broken 'between' when we just see "Highbury & Islington and King's Cross"
        BY("By"),
        SOUTH_OF("South of"),
        NORTH_OF("North of"),
        LEFT("Left"),
        LEAVING_TOWARDS("Leaving", "towards"),
        LEAVING("Leaving"),
        APPROACHING("Approaching"),
        TOWARDS("towards");

        private final String[] stringsToFind;

        /**
         * we don't need to do any parsing, this is the case of 'At Platfrom'
         */
        private final boolean returnStationAt;
 

        HtmlTrainStates(String... htmlStrings) {
            this.stringsToFind = htmlStrings;
            this.returnStationAt = false;
        }

        HtmlTrainStates(boolean returnStationAt, String... htmlStrings) {
            this.stringsToFind = htmlStrings;
            this.returnStationAt = returnStationAt;
        }

        /**
         * TODO these matches should at a space after each string in stringsToFind
         */
        public List<String> matches(String htmlPosition, String stationAt) {
            if (stringsToFind.length == 1 && (htmlPosition.indexOf(stringsToFind[0]) > -1)) {
                if (returnStationAt) return Collections.singletonList(stationAt);
                return Collections.singletonList(closeToAStation(stringsToFind[0], htmlPosition));
            } else if (stringsToFind.length == 2 && (htmlPosition.indexOf(stringsToFind[0]) > -1)) {
                String[] strings = htmlPosition.substring(stringsToFind[0].length()).split(stringsToFind[1]); // beween, and
                return Arrays.asList(StringUtils.trimAll(strings));
            } else {
                return Collections.emptyList();
            }
        }

        private String closeToAStation(String descriptor, String info) {
            final String startStation = info.substring(descriptor.length(), info.length());
            String result = startStation;
            if (result.indexOf(Constants.PLATFORM) > -1) {
                result = startStation.substring(0, startStation.indexOf(Constants.PLATFORM));
            }

            //deal with 'Waterloo Platfrom 5'
            if (org.apache.commons.lang.StringUtils.isBlank(result)) {
                result = info.substring(0, descriptor.length());
            }

            return StringUtils.trim(result);
        }
    }
}
