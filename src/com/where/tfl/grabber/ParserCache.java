package com.where.tfl.grabber;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Charles Kubicek
 */
public class ParserCache {

    private final Logger LOG = Logger.getLogger(ParserCache.class);

    private static final File OUT_DIR = new File("C:\\data\\projects\\wheresmytube\\recorded");
    private long id;

    public ParserCache(){
        id = System.currentTimeMillis();
    }

    public void add(String html, String info){
        try{
            IOUtils.write(html, new FileOutputStream(OUT_DIR+File.separator+id+"-"+info+".txt"));
        }   catch(IOException e){
            LOG.warn("didn't write because "+e.getMessage());
        }
    }
}
