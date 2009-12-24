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
    private final LineDao lineDao;

    public BoardParsing(DaoFactory daoFactory) {
        this.lineDao = daoFactory.getLineDao();
        this.stationValidation = new StationValidation(daoFactory);
    }

//    private BranchStop validateStation(String station, Branch branch) {
//        // test to see fi the station is on this line
////        if (lineDao.isStationOnLineAndNotOnBranch(branch, station)) {
////            return null;
////        }
//
//        BranchStop stop = stationValidation.vaidateStation(station, branch);
//        System.out.println("BoardParsing.validateStation validated station "+station +" as "+stop);
//
//        if (stop == null) {
//            LOG.warn("could not validate station with name '" + station + "' on branch '"+branch.getName()+"', returning null");
//            return null;
//        }
//        return stop;
//    }

    public DiscoveredTrain findPosition(String html_position, String stationName, Direction direction, Branch branch) {
        List<String> stringList = parse(html_position, stationName);
        String firstStation = stringList.get(0);

        FindBranchStopResult first = stationValidation.vaidateStation(firstStation, branch);

        if (!first.hasResult()) {
            return null;
        } else if (stringList.size() == 1) {
            return new DiscoveredTrain(Point.newPoint(first.getResult(), direction, html_position), first.getResult(), false);
        } else {
            FindBranchStopResult second = stationValidation.vaidateStation(stringList.get(1), branch);

            if (!second.hasResult()) {
                return new DiscoveredTrain(Point.newPoint(first.getResult(), direction, html_position), first.getResult(), false);
            } else {
                return makeDiscoveredTrain(html_position, direction, first.getResult(), second.getResult());
            }
        }
    }

    private DiscoveredTrain makeDiscoveredTrain(String html_position, Direction direction, BranchStop first, BranchStop second) {
        double firstY = first.getStation().getY();
        double secondY = second.getStation().getY();
        double firstX = first.getStation().getX();
        double secondX = second.getStation().getX();
        return new DiscoveredTrain(Point.newPoint(secondY - ((secondY - firstY) / 2),
                firstX + ((secondX - firstX) / 2), direction, html_position), first, true);
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
     * TODO use TrainLocation as a tuple instead of returning a list of strings
     */
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
