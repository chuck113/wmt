package com.where.dao.hsqldb;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.where.hibernate.Station;
import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.where.hibernate.TflStationCode;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.LinkedHashMultimap;

/**
 * Loads all objects form database
 */
public class HibernateHsqlLoader implements DataLoader{

  private static final Logger LOG = Logger.getLogger(HibernateHsqlLoader.class);    

  protected final Map<Branch, List<BranchStop>> branchesToBranchStops;
  protected final Map<BranchStop, Branch> branchStopsToBranches;
  protected final Map<String, Branch> branchNamesToBranches;
  protected final Map<String, BranchStop> stationNamesToBranchStops;
  protected final LinkedHashMultimap<String, Branch> lineNamesToBranches;

  private HibernateHsqlLoader() {
    this.branchesToBranchStops = new HashMap<Branch, List<BranchStop>>();
    this.branchStopsToBranches = new HashMap<BranchStop, Branch>();
    this.branchNamesToBranches = new HashMap<String, Branch>();
    this.stationNamesToBranchStops = new HashMap<String, BranchStop>();
    this.lineNamesToBranches = LinkedHashMultimap.create();
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
    
    for (Branch branch : branches) {
      List<BranchStop> result = session.createQuery("from BranchStop as bs where bs.branchId = '" + branch.getId() + "' order by orderNo").list();

      this.lineNamesToBranches.put(branch.getLine(), branch);  
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
        stationNamesToBranchStops.put(station.getName(), branchStop);
        //stationsToCodes.put(station, code);
        LOG.info("HibernateHsqlLoader.load "+branch.getName()+", "+branchStop.getStation().getName()+", "+branchStop.getOrderNo());
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

    public Map<String, BranchStop> getStationNamesToBranchStops() {
        return stationNamesToBranchStops;
    }

    public LinkedHashMultimap<String, Branch> getLineNamesToBranches() {
        return lineNamesToBranches;
    }
}
