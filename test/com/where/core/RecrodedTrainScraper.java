package com.where.core;

import com.where.tfl.grabber.TrainScraper;
import com.where.tfl.grabber.BoardParserResult;
import com.where.tfl.grabber.ParseException;
import com.where.tfl.grabber.TagSoupParser;
import com.where.domain.BranchStop;
import com.where.domain.Branch;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

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
            URL url = stationNameToRecrods.get(branchStop.getStation().getName()).toURI().toURL();
            String rawHtml = IOUtils.toString(url.openStream());

            return parser.parse(rawHtml);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }  catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
