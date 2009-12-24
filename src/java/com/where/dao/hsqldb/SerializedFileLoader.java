package com.where.dao.hsqldb;

import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;
import com.google.common.collect.Multimap;
import com.google.common.collect.LinkedHashMultimap;

import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.net.URISyntaxException;

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
    protected final Map<String, BranchStop> stationNamesToBranchStops;
    protected final LinkedHashMultimap<String, Branch> lineNamesToBranches;

    private static SerializedFileLoader INSTANCE;

    public static class Factory {

        /** uses the prefix: "serailized-tube-data/"  and the current threads context classloader */
        public static SerializedFileLoader fromClassPath() {
            return fromClassPath(Thread.currentThread().getContextClassLoader(), SERIALIZED_DATA_FOLDER);
        }

        public static SerializedFileLoader fromClassPath(ClassLoader classLoader, String prefix) {
            if(INSTANCE == null){
                synchronized (SerializedFileLoader.class){
                    if(INSTANCE == null){
                       INSTANCE  = new SerializedFileLoader(classLoader, prefix);
                    }
                }
            }

            return INSTANCE;
        }

        public static SerializedFileLoader fromFolder(String dataFolder) {
            return fromClassPath(Thread.currentThread().getContextClassLoader(), dataFolder);
        }
    }

    private SerializedFileLoader(ClassLoader classLoader, String prefix) {
        this.branchesToBranchStops = loadFromFile(makeFile(classLoader, prefix, BRANCHES_TO_BRANCH_STOPS_SER));
        this.branchStopsToBranches = loadFromFile(makeFile(classLoader, prefix, BRANCH_STOPS_TO_BRANCHES_SER));
        this.branchNamesToBranches = loadFromFile(makeFile(classLoader, prefix, BRANCH_NAMES_TO_BRANCHES_SER));
        this.stationNamesToBranchStops = loadFromFile(makeFile(classLoader, prefix, STATION_NAMES_TO_BRANCH_STOPS_SER));
        this.lineNamesToBranches = loadFromFile(makeFile(classLoader, prefix, LINE_NAMES_TO_BRANCHES_SER));
        assertMapSizes();
    }

    private void assertMapSizes(){
        assert (branchesToBranchStops.size() == 9);
        assert (branchStopsToBranches.size() == 147);
        assert (branchNamesToBranches.size() == 9);
        assert (stationNamesToBranchStops.size() == 104);
        assert (lineNamesToBranches.size() == 3);
    }

    private File makeFile(ClassLoader classLoader, String folder, String fileName) {
        try {
            return new File(classLoader.getResource(folder + fileName).toURI());
        } catch (URISyntaxException e) {
            LOG.error("while loading data file " + folder + "/" + fileName, e);
            throw new RuntimeException("while loading data file " + folder + "/" + fileName, e);
        }
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

    public Map<String, BranchStop> getStationNamesToBranchStops() {
        return stationNamesToBranchStops;
    }

    public LinkedHashMultimap<String, Branch> getLineNamesToBranches() {
        return lineNamesToBranches;
    }
}
