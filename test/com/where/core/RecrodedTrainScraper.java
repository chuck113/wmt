package com.where.core;

import com.where.tfl.grabber.TrainScraper;
import com.where.tfl.grabber.BoardParserResult;
import com.where.tfl.grabber.ParseException;
import com.where.tfl.grabber.TagSoupParser;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Branch;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Charles Kubicek
 */
public class RecrodedTrainScraper implements TrainScraper{

    private final Map<String, File> stationNameToRecrods;
    private final TagSoupParser parser;

    public RecrodedTrainScraper(File recordingsFolder){
        this.parser = new TagSoupParser();
        stationNameToRecrods = new HashMap<String, File>();

        File[] files = recordingsFolder.listFiles();
        for(File f: files){
            String stationNameWithDotTxt = (f.getName().split("-")[1]);
            String stationName = stationNameWithDotTxt.substring(0, (stationNameWithDotTxt.length()-4));
            stationNameToRecrods.put(stationName, f);
        }
    }

    public BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException {
        try {
            return parser.parse(stationNameToRecrods.get(branchStop.getStation().getName()).toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
