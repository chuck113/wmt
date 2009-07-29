package com.where.tools;

import com.where.dao.HibernateHsqlLoader;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Charles Kubicek
 *
 * Run this class to take data stored in the wmtdb database files and persist them as java
 * data objects. 
 */
public class HibernateDataSerializer {

    private static String TARGET_FOLDER = "serailized-tube-data";

    public static void main(String[] args) {
        HibernateHsqlLoader loader = HibernateHsqlLoader.instance();
        System.out.println("HibernateDataSerializer.main : "+new File(TARGET_FOLDER).getAbsolutePath());

        serialize(loader.getBranchesToBranchStops(), "branchesToBranchStops.ser");
        serialize(loader.getStationNamesToBrancheStops(), "stationNamesToBrancheStops.ser");
        serialize(loader.getBranchNamesToBranches(), "branchNamesToBranches.ser");
        serialize(loader.getBranchStopsToBranches(), "branchStopsToBranches.ser");
    }

    private static void serialize(Object obj, String fileName){
        try {
            System.out.println("HibernateDataSerializer writing to "+(new File(TARGET_FOLDER, fileName)));
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(TARGET_FOLDER, fileName)));

            os.writeObject(obj);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
