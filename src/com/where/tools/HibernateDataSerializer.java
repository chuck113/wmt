package com.where.tools;

import com.where.dao.hsqldb.HibernateHsqlLoader;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 *
 * Run this class to take data stored in the wmtdb database files and persist them as java
 * data objects. 
 */
public class HibernateDataSerializer {

    private static final Logger LOG = Logger.getLogger(HibernateDataSerializer.class);
    private static String TARGET_FOLDER = "serailized-tube-data";
    private static boolean TEST_RUN = true;

    public static void main(String[] args) {
        HibernateHsqlLoader loader = HibernateHsqlLoader.instance();
        LOG.info("HibernateDataSerializer.main : "+new File(TARGET_FOLDER).getAbsolutePath());

        serialize(loader.getBranchesToBranchStops(), "branchesToBranchStops.ser");
        serialize(loader.getStationNamesToBrancheStops(), "stationNamesToBrancheStops.ser");
        serialize(loader.getBranchNamesToBranches(), "branchNamesToBranches.ser");
        serialize(loader.getBranchStopsToBranches(), "branchStopsToBranches.ser");
        //serialize(loader.getStationsToCodes(), "stationsToCodes.ser");
    }

    private static void serialize(Object obj, String fileName){
        if(TEST_RUN)return;
        try {
            LOG.info("HibernateDataSerializer writing to "+(new File(TARGET_FOLDER, fileName)));
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(TARGET_FOLDER, fileName)));

            os.writeObject(obj);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
