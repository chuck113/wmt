package com.where.tfl.grabber;

import org.apache.commons.io.IOUtils;
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
    private static final File OUT_DIR = new File(USER_DIR+ File.separator+"recorded");
    private long id;

    public ParserPersistenceCache(){
        id = System.currentTimeMillis();
    }


    public void add(String html, String stationName){
        try{
            IOUtils.write(html, new FileOutputStream(OUT_DIR+File.separator+id+"-"+stationName+".txt"));
        }   catch(IOException e){
            LOG.warn("didn't write because "+e.getMessage());
        }
    }


}