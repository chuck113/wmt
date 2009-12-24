package com.where.tools;

import com.where.dao.hsqldb.HibernateHsqlLoader;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import static com.where.dao.hsqldb.DataLoader.*;

/**
 * @author Charles Kubicek
 *
 * Run this class to take data stored in the wmtdb database files and persist them as java
 * data objects. 
 */
public class HibernateDataSerializer {

    private static final Logger LOG = Logger.getLogger(HibernateDataSerializer.class);
    private static String TARGET_FOLDER = "serailized-tube-data";
    private static boolean TEST_RUN = false;//true;
    public static void main(String[] args) {
        HibernateHsqlLoader loader = HibernateHsqlLoader.instance();
        LOG.info("HibernateDataSerializer.main : "+new File(TARGET_FOLDER).getAbsolutePath());
        
        new File(TARGET_FOLDER).getAbsoluteFile().mkdirs();

        serialize(loader.getBranchesToBranchStops(), BRANCHES_TO_BRANCH_STOPS_SER);
        serialize(loader.getStationNamesToBranchStops(), BRANCH_STOPS_TO_BRANCHES_SER);
        serialize(loader.getBranchNamesToBranches(), BRANCH_NAMES_TO_BRANCHES_SER);
        serialize(loader.getBranchStopsToBranches(), STATION_NAMES_TO_BRANCH_STOPS_SER);
        serialize(loader.getLineNamesToBranches(), LINE_NAMES_TO_BRANCHES_SER);
    }

    private static void serialize(Object obj, String fileName){
        if(TEST_RUN)return;
        try {
            LOG.info("HibernateDataSerializer writing to "+(new File(TARGET_FOLDER, fileName).getAbsoluteFile()));
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(TARGET_FOLDER, fileName).getAbsoluteFile()));

            os.writeObject(obj);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
