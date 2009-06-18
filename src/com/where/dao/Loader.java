package com.where.dao;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.where.dao.hibernate.Station;
import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.TflStationCode;

/**
 * Loads all objects form database
 */
public class Loader {

  private final Map<Branch, List<BranchStop>> branchesToBranchStops;
  private final Map<BranchStop, Branch> branchStopsToBranches;
  private final Map<String, Branch> branchNamesToBranches;
  private final Map<String, BranchStop> stationNamesToBrancheStops;

  private Loader() {
    this.branchesToBranchStops = new HashMap<Branch, List<BranchStop>>();
    this.branchStopsToBranches = new HashMap<BranchStop, Branch>();
    this.branchNamesToBranches = new HashMap<String, Branch>();
    this.stationNamesToBrancheStops = new HashMap<String, BranchStop>();
    load();
  }

  private static Loader loader = null;

  public static Loader instance() {
    if (loader == null)
      synchronized (Loader.class) {
        if (loader == null) {
          loader = new Loader();
        }
      }
    return loader;
  }

  public List<BranchStop> getBranchStops(Branch branch) {
    return branchesToBranchStops.get(branch);
  }

  private void load() {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    Session session = sessionFactory.openSession();

    List<Branch> branches = session.createQuery("from Branch").list();
      System.out.println("Loader.load branches: "+branches);

    for (Branch branch : branches) {
      List<BranchStop> result = session.createQuery("from BranchStop as bs where bs.branchId = '" + branch.getId() + "' order by orderNo").list();
      //System.out.println("branch: " + branch.getName());

      this.branchNamesToBranches.put(branch.getName(), branch);

      for (BranchStop branchStop : result) {
        //@todo, branchStop.getStationId() should return int
        this.branchStopsToBranches.put(branchStop, branch);
        System.out.println("branchStop: " + branchStop.getStationId());
        Station station = (Station) session.load(Station.class, Integer.parseInt(branchStop.getStationId()));
        branchStop.setStation(station);

        List list = session.createQuery("from TflStationCode as code where code.stationId = :stationId")
                .setParameter("stationId", Integer.toString(station.getId()))
                .list();

        TflStationCode code = (TflStationCode) list.get(0);
        branchStop.setStationCode(code);
        stationNamesToBrancheStops.put(station.getName(), branchStop);
        //System.out.println("code: " + code.getCode());
      }

      this.branchesToBranchStops.put(branch, result);
    }

    session.close();
    //List<Stations> stations = session.createQuery("from Stations").list();
  }

  public Map<BranchStop, Branch> getBranchStopsToBranches() {
    return branchStopsToBranches;
  }

  public BranchStop getBranchStopFromStationName(String stationName){
     return stationNamesToBrancheStops.get(stationName);
  }

  public Map<String, Branch> getBranchNamesToBranches() {
    return branchNamesToBranches;
  }

}
