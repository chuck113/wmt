package com.where.core;

import com.where.dao.*;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.tfl.grabber.TFLScraper;
import com.where.tfl.grabber.ParseException;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The algorith starts at the station after the start of a branch (the station at the end)
 * doesn't need to show which trains are coming), goes to the other end, switches
 * direction then goes back to the station before the station started at
 */
public class Algorithm implements Runnable {

  private Logger LOG = Logger.getLogger(Algorithm.class);

  private final String branch;
  private final Loader loader;
  private final TFLScraper scraper;

  static private final int SCRAPER_RETRIES = 3;

  // the directions in which we traverse the line, does not coralate to the board directions we read.
  //@@universal_traversal_directions = [Universal_direction.One, Universal_direction.Two]

  public Algorithm(String branch) {
    this.branch = branch;
    this.loader = Loader.instance();
    this.scraper = new TFLScraper();
  }


  public void run() {
    Branch branch = loader.getBranchNamesToBranches().get(this.branch);

    List<AbstractDirection> abstractDirections = Arrays.asList(AbstractDirection.values());

    for (AbstractDirection direction : abstractDirections) {
      iterateForDirection(branch, direction);
    }


  }

  void iterateForDirection(Branch branch, AbstractDirection direction) {
    List<BranchStop> branchStops = loader.getBranchStops(branch);

    BranchStop endpoint;
    BranchStop currentStop;

    if (direction == AbstractDirection.ONE) {
      endpoint = branchStops.get(0);
      currentStop = branchStops.get(branchStops.size() - 2);
    } else {
      endpoint = branchStops.get(branchStops.size() - 1);
      currentStop = branchStops.get(1);
    }

    System.out.println("endpoint = '" + endpoint.getStation().getName() + "'");
    System.out.println("startpoint = '" + currentStop.getStation().getName() + "'");

    int furthestTime = 0;
    int validStations;
    int iterationCount = 0; // to stop infinite loops
    BranchStop furthestStation = currentStop;

    List<Position> positions = new ArrayList<Position>();
    List<Point> mapPoints = new ArrayList<Point>();

    while (furthestStation != null && (!furthestStation.equals(endpoint)) && iterationCount++ < 6) {
      System.out.println("furthest: " + furthestStation.getStation().getName());

      //#@logger.info "starting loop using stop: " + branch_stop.station.name
      furthestTime = 0; // if time is zero means train is at station and we will have already added
      //# our station in the last parse, (unless the train moved on?)
      validStations = 0;
      furthestStation = null;

      List<TimeInfo> timeInfo = getNextStop(direction, currentStop);

      if (timeInfo != null && timeInfo.size() > 0) {

        for (TimeInfo info : timeInfo) {
          System.out.println("info = " + info);

          if(info.getInfo().length() > 0){            
            Position position = makePosition(info.getInfo(), currentStop.getStation().getName());
            positions.add(position);
            mapPoints.add(position.makeMidwayPoint());
          } else {
            //TODO cope with no info; estimate the position
          }
        }

        currentStop = furthestStation = positions.get(positions.size() - 1).findFurthest();
      }
    }

    System.out.println("finished");
  }

  private BranchStop findFurthestStation(){
    return null;
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
    if (startStation.indexOf(Constants.PALTFORM) > -1) {
      startStation = startStation.substring(0, startStation.indexOf(Constants.PALTFORM));
    }
    return StringUtils.trim(startStation);
  }

  public Position makePosition(String html_position, String stationName) {

    String startStation = null;
    String endStation = null;

    //'Between High Barnet and Totteridge & Whetstone'
    if (html_position.indexOf(Constants.BETWEEN) > -1) {
      String[] strings = html_position.substring(Constants.BETWEEN.length()).split(Constants.AND);

      startStation = StringUtils.trim(strings[0]);
      endStation = StringUtils.trim(strings[1]);
    } else if (html_position.indexOf(Constants.AT_PALTFORM) > -1) {
      startStation = stationName;
    } else if (html_position.indexOf(Constants.AT) > -1) {  //At East Finchley Platform 4
      startStation = closeToAStation(Constants.AT, html_position);
    } else if (html_position.indexOf(Constants.BY) > -1) {  //At East Finchley Platform 4
      startStation = closeToAStation(Constants.BY, html_position);
    } else if (html_position.indexOf(Constants.LEFT) > -1) {  //At East Finchley Platform 4
      startStation = closeToAStation(Constants.LEFT, html_position);
    } else
    if (html_position.indexOf(Constants.LEAVING) > -1 && html_position.indexOf(Constants.TOWARDS) > -1) {  //At East Finchley Platform 4
      LOG.warn("leaving towards not implemented propertly");
      startStation = closeToAStation(Constants.LEAVING, html_position);
      // not implemented
    } else if (html_position.indexOf(Constants.LEAVING) > -1) {  //At East Finchley Platform 4
      startStation = closeToAStation(Constants.LEAVING, html_position);
    } else if (html_position.indexOf(Constants.APPROACHING) > -1) {  //At East Finchley Platform 4
      startStation = closeToAStation(Constants.APPROACHING, html_position);
    } else if (html_position.length() > 0) { // have seen just the station name, or just the station name and platofrm X
      if (html_position.indexOf(Constants.PALTFORM) > -1) {
        startStation = html_position.split(Constants.PALTFORM)[0];
      } else {
        startStation = html_position;
      }
    }

//    elsif html_position.length > 0 # have seen just the station name, or just the station name and platofrm X
//      if html_position.index(@@PLATFORM) != nil
//        start_station = html_position.split(@@PLATFORM)[0]
//      else
//        start_station = html_position
//      end
//      to_return =  Position.new(html_position, nil)
//    else

    BranchStop start = null;
    BranchStop end = null;

    if (startStation != null) {
      start = vaidateStation(startStation);
    }

    if (endStation != null) {
      end = vaidateStation(endStation);
    }

    return new Position(start, end);
  }

  // def get_next_stop(branch, direction, current_stop, branch_stops_array)

  /**
   * Strips known suffixes off stations and gets the equivalien branchStop
   *
   * @param station
   * @param suffix
   * @return
   */
  private BranchStop stripSuffix(String station, String suffix) {
    if (station.endsWith("Station")) {
      return loader.getBranchStopFromStationName(station.substring(0, station.length() - suffix.length() - 1));
    }

    return null;
  }

  private BranchStop vaidateStation(String station) {
    if (station == null) return null;

    BranchStop stop = loader.getBranchStopFromStationName(station);

    if (stop != null) {
      return stop;
    }
//
//    if (stop == null) {
//      if (station.endsWith("Station")) {
//        stop = loader.getBranchStopFromStationName(station.substring(0, station.length() - "Station".length() - 1));
//      }
//    }

    String[] suffixes = new String[]{"Station", "Siding", "Depot"};

    for (int i = 0; i < suffixes.length && stop == null; i++) {
      String suffix = suffixes[i];
      stop = stripSuffix(station, suffix);
    }

    if (stop == null) {
      String alt = alternateNames(station);
      stop = loader.getBranchStopFromStationName(station);

      if (stop == null) {
        LOG.warn("didn't find station for string: '" + station + "'");
      }
    }

    return stop;
  }
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

  private String alternateNames(String name) {
    if (name == "King's Cross")
      return "King's Cross St. Pancras";
    else if (name == "Elephant &amp; Castle")
      return "Elephant & Castle";
    else if (name == "Totteridge &amp; Whetstone")
      return "Totteridge & Whetstone";

    return null;
  }

  private List<TimeInfo> getNextStop(
          /*Branch branch,*/ AbstractDirection direction, BranchStop branchStop/*, List<BranchStop> branchStops*/) {

    int attempts = 0;

    while (attempts < SCRAPER_RETRIES) {

      try {
        Map<String, List<TimeInfo>> data = scraper.get(branchStop);
        DirectionName concreteDirection = direction.getConcreteDirection(new ArrayList<String>(data.keySet()));

        return data.get(concreteDirection.getName());
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