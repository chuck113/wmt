package com.where.dao.hsqldb;

import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;

import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class SerializedFileLoader implements DataLoader {

    private final Logger LOG = Logger.getLogger(SerializedFileLoader.class);
    public static final String DATA_FOLDER_NAME = "serailized-tube-data";
    public static final String SERIALIZED_DATA_FOLDER = "serailized-tube-data/";

    protected final Map<Branch, List<BranchStop>> branchesToBranchStops;
    protected final Map<BranchStop, Branch> branchStopsToBranches;
    protected final Map<String, Branch> branchNamesToBranches;
    protected final Map<String, BranchStop> stationNamesToBrancheStops;

    public static class Factory {

        /** uses the prefix: "serailized-tube-data/"  and the current threads context classloader */
        public static SerializedFileLoader fromClassPath() {
            return new SerializedFileLoader(Thread.currentThread().getContextClassLoader(), SERIALIZED_DATA_FOLDER);
        }

        public static SerializedFileLoader fromClassPath(ClassLoader classLoader, String prefix) {
            return new SerializedFileLoader(classLoader, prefix);
        }

        public static SerializedFileLoader fromFolder(String dataFolder) {
            return new SerializedFileLoader(dataFolder);
        }
    }

    private SerializedFileLoader(ClassLoader classLoader, String prefix) {
        this.branchesToBranchStops = loadFromFile(makeFile(classLoader, prefix, "branchesToBranchStops.ser"));
        this.branchStopsToBranches = loadFromFile(makeFile(classLoader, prefix, "branchStopsToBranches.ser"));
        this.branchNamesToBranches = loadFromFile(makeFile(classLoader, prefix, "branchNamesToBranches.ser"));
        this.stationNamesToBrancheStops = loadFromFile(makeFile(classLoader, prefix, "stationNamesToBrancheStops.ser"));
        assertMapSizes();
    }

    private void assertMapSizes(){
        assert (branchesToBranchStops.size() == 9);
        assert (branchStopsToBranches.size() == 147);
        assert (branchNamesToBranches.size() == 9);
        assert (stationNamesToBrancheStops.size() == 104);
    }

    private File makeFile(ClassLoader classLoader, String folder, String fileName) {
        try {
            return new File(classLoader.getResource(folder + fileName).toURI());
        } catch (URISyntaxException e) {
            LOG.error("while loading data file " + folder + "/" + fileName, e);
            throw new RuntimeException("while loading data file " + folder + "/" + fileName, e);
        }
    }

    private SerializedFileLoader(String dataFolder) {
        this.branchesToBranchStops = loadFromFile(new File(dataFolder, "branchesToBranchStops.ser"));
        this.branchStopsToBranches = loadFromFile(new File(dataFolder, "branchStopsToBranches.ser"));
        this.branchNamesToBranches = loadFromFile(new File(dataFolder, "branchNamesToBranches.ser"));
        this.stationNamesToBrancheStops = loadFromFile(new File(dataFolder, "stationNamesToBrancheStops.ser"));
        assertMapSizes();
    }

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
