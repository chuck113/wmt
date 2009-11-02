package com.where.tfl.grabber;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Charles Kubicek
 */
public class ParserPersistenceCache {

    private final Logger LOG = Logger.getLogger(ParserPersistenceCache.class);

    private static final String USER_DIR = System.getProperty("user.dir");
    private final File outDir;
    private String prefix;

    public ParserPersistenceCache(String prefix, String targetFolderName){
        this.prefix = StringUtils.isEmpty(prefix)?""+System.currentTimeMillis():prefix+"-"+System.currentTimeMillis();
        outDir = new File(USER_DIR+ File.separator+targetFolderName);
        System.out.println("saving results to "+ outDir +" with prefix '"+ this.prefix+"'");

        outDir.mkdirs();
    }

    public ParserPersistenceCache(){
        this("", "recorded");
    }


    public void add(String html, String stationName){
        try{
            IOUtils.write(html, new FileOutputStream(outDir +File.separator+ prefix +"-"+stationName+".html"));
        }   catch(IOException e){
            LOG.warn("didn't write because "+e.getMessage());
        }
    }


}