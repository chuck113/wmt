package com.where.dao.hsqldb;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.where.hibernate.Station;
import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.where.hibernate.TflStationCode;

/**
 * Loads all objects form database
 */
public class HibernateHsqlLoader implements DataLoader{

  protected final Map<Branch, List<BranchStop>> branchesToBranchStops;
  protected final Map<BranchStop, Branch> branchStopsToBranches;
  protected final Map<String, Branch> branchNamesToBranches;
  protected final Map<String, BranchStop> stationNamesToBrancheStops;
  //protected final Map<Station, TflStationCode> stationsToCodes;

  private HibernateHsqlLoader() {
    this.branchesToBranchStops = new HashMap<Branch, List<BranchStop>>();
    this.branchStopsToBranches = new HashMap<BranchStop, Branch>();
    this.branchNamesToBranches = new HashMap<String, Branch>();
    this.stationNamesToBrancheStops = new HashMap<String, BranchStop>();
    //this.stationsToCodes = new HashMap<Station, TflStationCode>();
    load();
  }

  private static HibernateHsqlLoader loader = null;

    public static HibernateHsqlLoader instance() {
    if (loader == null)
      synchronized (HibernateHsqlLoader.class) {
        if (loader == null) {
          loader = new HibernateHsqlLoader();
        }
      }
    return loader;
  }


  private void load() {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    Session session = sessionFactory.openSession();

    List<Branch> branches = session.createQuery("from Branch").list();
    //System.out.println("HibernateHsqlLoader.load branches: "+branches);

    for (Branch branch : branches) {
      List<BranchStop> result = session.createQuery("from BranchStop as bs where bs.branchId = '" + branch.getId() + "' order by orderNo").list();

      this.branchNamesToBranches.put(branch.getName(), branch);

      for (BranchStop branchStop : result) {
        //@todo, branchStop.getStation() should return int
        this.branchStopsToBranches.put(branchStop, branch);
        Station station = (Station) session.load(Station.class, Integer.parseInt(branchStop.getStationId()));
        branchStop.setStation(station);

        List list = session.createQuery("from TflStationCode as code where code.stationId = :stationId")
                .setParameter("stationId", Integer.toString(station.getId())).list();

        TflStationCode code = (TflStationCode) list.get(0);
        branchStop.setStationCode(code);
        stationNamesToBrancheStops.put(station.getName(), branchStop);
        //stationsToCodes.put(station, code);
          System.out.println("HibernateHsqlLoader.load "+branch.getName()+", "+branchStop.getStation().getName()+", "+branchStop.getOrderNo());
      }

      this.branchesToBranchStops.put(branch, result);
    }

    session.close();
    //List<Stations> stations = session.createQuery("from Stations").list();
  }

  public Map<BranchStop, Branch> getBranchStopsToBranches() {
    return branchStopsToBranches;
  }

    public Map<Branch, List<BranchStop>> getBranchesToBranchStops() {
        return branchesToBranchStops;
    }

    public Map<String, Branch> getBranchNamesToBranches() {
        return branchNamesToBranches;
    }

    public Map<String, BranchStop> getStationNamesToBrancheStops() {
        return stationNamesToBrancheStops;
    }

//    public Map<Station, TflStationCode> getStationsToCodes() {
//        return stationsToCodes;
//    }

    //  public BranchStop getBranchStopFromStationName(String stationName){
//     return stationNamesToBrancheStops.get(stationName);
//  }
//
//  public Map<String, Branch> getBranchNamesToBranches() {
//    return branchNamesToBranches;
//  }
//
//  public List<BranchStop> getBranchStops(Branch branch) {
//    return branchesToBranchStops.get(branch);
//  }

}
