package com.where.domain.alg;

import com.where.dao.*;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.tfl.grabber.TFLScraper;
import com.where.tfl.grabber.ParseException;
import com.where.domain.Point;
import com.where.domain.Position;
import com.where.domain.Direction;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The algorith starts at the station after the start of a branch (the station at the end)
 * doesn't need to show which trains are coming), goes to the other end, switches
 * direction then goes back to the station before the station started at
 */
public class Algorithm {

    private Logger LOG = Logger.getLogger(Algorithm.class);

    private final String branch;
    private final DataMapper dataMapper;
    private final TFLScraper scraper;
    private final BoardParsing boardParsing;

    static private final int SCRAPER_RETRIES = 3;

    // the directions in which we traverse the line, does not coralate to the board directions we read.
    //@@universal_traversal_directions = [Universal_direction.One, Universal_direction.Two]

    public Algorithm(String branch, DataMapper dataMapper) {
        this.branch = branch;
        this.dataMapper = dataMapper;
        this.scraper = new TFLScraper();
        this.boardParsing = new BoardParsing(dataMapper);
    }


    public Set<Point> run() {
        Branch branch = dataMapper.getBranchNamesToBranches().get(this.branch);
        Set<Point> result = new HashSet<Point>();
        List<AbstractDirection> abstractDirections = Arrays.asList(AbstractDirection.values());

        for (AbstractDirection direction : abstractDirections) {
            result.addAll(iterateForDirection(branch, direction));
        }

        return result;
    }

    List<Point> iterateForDirection(Branch branch, AbstractDirection direction) {
        List<BranchStop> branchStops = dataMapper.getBranchStops(branch);

        BranchStop endpoint;
        BranchStop currentStop;

        if (direction == AbstractDirection.ONE) {
            endpoint = branchStops.get(0);
            currentStop = branchStops.get(branchStops.size() - 2);
        } else {
            endpoint = branchStops.get(branchStops.size() - 1);
            currentStop = branchStops.get(1);
        }

        LOG.info("endpoint = '" + endpoint.getStation().getName() + "'");
        LOG.info("startpoint = '" + currentStop.getStation().getName() + "'");

        int furthestTime = 0;
        int validStations;
        int iterationCount = 0; // to stop infinite loops
        BranchStop furthestStation = currentStop;

        List<DiscoveredTrain> discoveredPoints = new ArrayList<DiscoveredTrain>();
        //Set<Point> mapPoints = new HashSet<Point>();

        while (furthestStation != null && (!furthestStation.equals(endpoint)) && iterationCount++ < 6) {
            LOG.info("furthest: " + furthestStation.getStation().getName());

            furthestTime = 0; // if time is zero means train is at station and we will have already added
            //# our station in the last parse, (unless the train moved on?)
            validStations = 0;
            furthestStation = null;

            BoardData boardData = getNextStop(branch, direction, currentStop);
            List<TimeInfo> timeInfo = boardData.timeInfo;

            for (TimeInfo info : timeInfo) {
                LOG.info("info = " + info);

                if (info.getInfo().length() > 0) {
                    //Position position = makePosition(info.getInfo(), currentStop.getStation().getName());
                    DiscoveredTrain discoveredPoint = boardParsing.findPosition(
                            info.getInfo(),
                            currentStop.getStation().getName(),
                            boardData.concreteDirection);
                    if(discoveredPoint != null)discoveredPoints.add(discoveredPoint);
                    //mapPoints.add(discoveredPoint.getPoint());
                } else {
                    LOG.warn("got no time info from stop " + currentStop.getStation().getName());
                    //TODO cope with no info; estimate the position
                }
            }

            currentStop = furthestStation = discoveredPoints.get(discoveredPoints.size() - 1).getFurthestStation();

        }

        LOG.info("finished");
        return Collections.<Point>unmodifiableList(discoveredPoints);
    }

    private <T extends Point> List<T> convert(List<? extends Point> input){
        return (List<T>)input;
    }

    private BranchStop findFurthestStation() {
        return null;
    }



    

//    private DiscoveredTrain findPosition(String html_position, String stationName, Direction direction){
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
//        return buildPoint(startStation, endStation, direction);
//    }
//
//    private DiscoveredTrain buildPoint(String firstStation, String secondStation, Direction direction) {
//        BranchStop first = vaidateStation(firstStation);
//
//        if (secondStation == null) {
//            return new DiscoveredTrain(new Point(first.getStation().getLat(), first.getStation().getLng(), direction), first);
//        } else {
//            BranchStop second = vaidateStation(secondStation);
//
//            return new DiscoveredTrain(new Point((second.getStation().getX() + first.getStation().getY()) / 2,
//                    (first.getStation().getY() + second.getStation().getY()) / 2,
//                    direction), second);
//        }
//    }


    /**
     * Given a parsed html read out such as 'Between High Barnet and Totteridge & Whetstone', returns a
     * {@link Position} object
     *
     * @param html_position
     * @param stationName
     * @return
     */
//    public Position makePosition(String html_position, String stationName) {
//
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
////    elsif html_position.length > 0 # have seen just the station name, or just the station name and platofrm X
////      if html_position.index(@@PLATFORM) != nil
////        start_station = html_position.split(@@PLATFORM)[0]
////      else
////        start_station = html_position
////      end
////      to_return =  Position.new(html_position, nil)
////    else
//
//        BranchStop start = null;
//        BranchStop end = null;
//
//        //TODO: do something cleverer here, validate station can return null,
//        // use NullBranchStop with appropiate charateristics
//        if (startStation != null) {
//            start = vaidateStation(startStation);
//        }
//
//        if (endStation != null) {
//            end = vaidateStation(endStation);
//        }
//
//        return new Position(start, end);
//    }

    // def get_next_stop(branch, direction, current_stop, branch_stops_array)

//    /**
//     * Strips known suffixes off stations and gets the equivalien branchStop
//     *
//     * @param station
//     * @param suffix
//     * @return
//     */
//    private BranchStop stripSuffix(String station, String suffix) {
//        if (station.endsWith("Station")) {
//            return dataMapper.getBranchStopFromStationName(station.substring(0, station.length() - suffix.length() - 1));
//        }
//
//        return null;
//    }

//    private BranchStop vaidateStation(String station) {
//        if (station == null) return null;
//
//        BranchStop stop = dataMapper.getBranchStopFromStationName(station);
//
//        if (stop != null) {
//            return stop;
//        }
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
//            stop = dataMapper.getBranchStopFromStationName(alternateNames(station));
//
//            if (stop == null) {
//                LOG.warn("didn't find station for string: '" + station + "'");
//            }
//        }
//
//        return stop;
//    }
//   def validate_station(station_name)
//    if station_name == nil
//      return nil
//    end
//
//    station = Station.find_by_name(station_name)
//    if (station == nil)
//      if (station_name.ends_with?("Station") )
//        station_name.strip_end!(" Station")
//        station = Station.find_by_name(station_name)
//      end
//    end
//
//    if (station == nil)
//      if (station_name.ends_with?("Siding") )
//        station_name.strip_end!(" Siding")
//        station = Station.find_by_name(station_name)
//      end
//    end
//
//    if (station == nil)
//      if (station_name.ends_with?("Depot") )
//        station_name.strip_end!(" Depot")
//        station = Station.find_by_name(station_name)
//      end
//    end
//
//    if (station == nil)
//      alt = alternate_names(station_name)
//      station = Station.find_by_name(alt)
//      if (station != nil)
//        @logger.info "found replacement " + alt + " for " + station_name
//        return station.name
//      end
//    else
//      return station.name
//    end
//
//    return nil
//  end




    /**
     * Data found on a board
     */
    private class BoardData{
       List<TimeInfo> timeInfo;
       Direction concreteDirection;

        private BoardData(List<TimeInfo> timeInfo, Direction concreteDirection) {
            this.timeInfo = timeInfo;
            this.concreteDirection = concreteDirection;
        }
    }

    private BoardData getNextStop(
            Branch branch, AbstractDirection direction, BranchStop branchStop/*, List<BranchStop> branchStops*/) {

        int attempts = 0;

        while (attempts < SCRAPER_RETRIES) {

            try {
                Map<String, List<TimeInfo>> data = scraper.get(branchStop, branch);
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(data.keySet()));

                return new BoardData(data.get(concreteDirection.getName()), concreteDirection);
            } catch (ParseException e) {
                attempts++;
                LOG.warn("failed to scrape, attempt no " + attempts, e);
                if (attempts == SCRAPER_RETRIES) {
                    LOG.error("failed to scrape after all attempts, bailing");
                    return null;
                }

                try {
                    Thread.sleep(3000 * attempts);
                } catch (InterruptedException e1) {
                    //ignore
                }
            }
        }
        // should never get here...
        return null;
    }


}