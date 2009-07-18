package com.where.domain.alg;

import com.where.domain.alg.Constants;
import com.where.domain.alg.StringUtils;
import com.where.domain.Direction;
import com.where.domain.Point;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.DataMapper;
import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class BoardParsing {

    private final Logger LOG = Logger.getLogger(BoardParsing.class);

    private final DataMapper dataMapper;
    private final StationValidation stationValidation;

    public BoardParsing(DataMapper dataMapper) {
        this.dataMapper = dataMapper;
        stationValidation = new StationValidation(dataMapper);
    }

    /**
     * A lot of info strings are very similar, i.e:
     * "At East Finchley Platform 4"
     * "By East Finchley Platform 4"
     * "Left East Finchley
     *
     * @param descriptor wheather it is "at", "by" etc
     * @param info       the scrapped string
     * @return
     */
    private String closeToAStation(String descriptor, String info) {
        String startStation = info.substring(descriptor.length(), info.length());
        if (startStation.indexOf(Constants.PLATFORM) > -1) {
            startStation = startStation.substring(0, startStation.indexOf(Constants.PLATFORM));
        }
        return StringUtils.trim(startStation);
    }


    public DiscoveredTrain findPosition(String html_position, String stationName, Direction direction){
        String startStation = null;
        String endStation = null;

        //'Between High Barnet and Totteridge & Whetstone'
        if (html_position.indexOf(Constants.BETWEEN) > -1) {
            String[] strings = html_position.substring(Constants.BETWEEN.length()).split(Constants.AND);

            startStation = StringUtils.trim(strings[0]);
            endStation = StringUtils.trim(strings[1]);
        } else if (html_position.indexOf(Constants.AT_PLATFORM) > -1) {
            startStation = stationName;
        } else if (html_position.indexOf(Constants.AT) > -1) {  //At East Finchley Platform 4
            startStation = closeToAStation(Constants.AT, html_position);
        } else if (html_position.indexOf(Constants.BY) > -1) {  //At East Finchley Platform 4
            startStation = closeToAStation(Constants.BY, html_position);
        } else if (html_position.indexOf(Constants.LEFT) > -1) {  //At East Finchley Platform 4
            startStation = closeToAStation(Constants.LEFT, html_position);
        } else if (html_position.indexOf(Constants.LEAVING) > -1 && html_position.indexOf(Constants.TOWARDS) > -1) {  //At East Finchley Platform 4
            LOG.warn("leaving towards not implemented properly");
            startStation = closeToAStation(Constants.LEAVING, html_position);
            // not implemented
        } else if (html_position.indexOf(Constants.LEAVING) > -1) {  //At East Finchley Platform 4
            startStation = closeToAStation(Constants.LEAVING, html_position);
        } else if (html_position.indexOf(Constants.APPROACHING) > -1) {  //At East Finchley Platform 4
            startStation = closeToAStation(Constants.APPROACHING, html_position);
        } else if (html_position.length() > 0) { // have seen just the station name, or just the station name and platofrm X
            if (html_position.indexOf(Constants.PLATFORM) > -1) {
                startStation = html_position.split(Constants.PLATFORM)[0];
            } else {
                startStation = html_position;
            }
        }

        return buildPoint(startStation, endStation, direction, html_position);
    }

    /** if there are two stations then the firsrt is the furthest away.
     * TODO this seems pretty wiered and we should work this out with the direction and alist of stops.
     * @param firstStation
     * @param secondStation
     * @param direction
     * @return
     */
    private DiscoveredTrain buildPoint(String firstStation, String secondStation, Direction direction, String description) {
        com.where.dao.hibernate.BranchStop first = stationValidation.vaidateStation(firstStation);

        if(first == null){
            LOG.warn("could not validate station with name '"+firstStation+"', returning null");
            return null;
        }

        if (secondStation == null) {
            return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction, description), first);
        } else {
            BranchStop second = stationValidation.vaidateStation(secondStation);

            if(second == null) {
                LOG.warn("could not validate station with name '"+secondStation+"', returning first station only");
                return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction, description), first);
            }

            return new DiscoveredTrain(new Point(
                    (second.getStation().getY()) - ((second.getStation().getY() - first.getStation().getY()) / 2),
                    (first.getStation().getX()) + ((second.getStation().getX() - first.getStation().getX()) / 2),
                    direction, description), first);
        }
    }

}
