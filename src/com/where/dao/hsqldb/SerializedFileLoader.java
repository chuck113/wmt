package com.where.dao.hsqldb;

import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.where.hibernate.Station;
import com.where.hibernate.TflStationCode;

import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class SerializedFileLoader implements DataLoader {

    private final Logger LOG = Logger.getLogger(SerializedFileLoader.class);
    public static String DATA_FOLDER_NAME = "serailized-tube-data";


    protected final Map<Branch, List<BranchStop>> branchesToBranchStops;
    protected final Map<BranchStop, Branch> branchStopsToBranches;
    protected final Map<String, Branch> branchNamesToBranches;
    protected final Map<String, BranchStop> stationNamesToBrancheStops;
    //protected final Map<Station, TflStationCode> stationsToCodes;

    public SerializedFileLoader(String dataFolder) {
        this.branchesToBranchStops = loadFromFile(new File(dataFolder, "branchesToBranchStops.ser"));
        this.branchStopsToBranches = loadFromFile(new File(dataFolder, "branchStopsToBranches.ser"));
        this.branchNamesToBranches = loadFromFile(new File(dataFolder, "branchNamesToBranches.ser"));
        this.stationNamesToBrancheStops = loadFromFile(new File(dataFolder, "stationNamesToBrancheStops.ser"));
//        this.stationsToCodes = loadFromFile(new File(dataFolder, "stationsToCodes.ser"));

        assert(branchesToBranchStops.size() == 9);
        assert(branchStopsToBranches.size() == 147);
        assert(branchNamesToBranches.size() == 9);
        assert(stationNamesToBrancheStops.size() == 104);
    }

//    private static SerializedFileLoader INSTANCE;
//
//    public static SerializedFileLoader instance() {
//        if (INSTANCE == null) {
//            synchronized (HibernateHsqlLoader.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = new SerializedFileLoader();
//                }
//            }
//        }
//        return INSTANCE;
//    }

    private <T> T loadFromFile(File file) {
        try {
            LOG.info("deseralizing file from: " + file.getAbsolutePath());
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            return (T) in.readObject();
        } catch (Exception e) {
            LOG.error("failed to deserialize file: " + file, e);
            return null;
        }
    }

    public Map<Branch, List<BranchStop>> getBranchesToBranchStops() {
        return branchesToBranchStops;
    }

    public Map<BranchStop, Branch> getBranchStopsToBranches() {
        return branchStopsToBranches;
    }

    public Map<String, Branch> getBranchNamesToBranches() {
        return branchNamesToBranches;
    }

    public Map<String, BranchStop> getStationNamesToBrancheStops() {
        return stationNamesToBrancheStops;
    }
//
//    public Map<Station, TflStationCode> getStationsToCodes() {
//        return stationsToCodes;
//    }
}
