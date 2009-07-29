package com.where.tfl.grabber;

import com.where.dao.hibernate.Branch;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.TflStationCode;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

/**
 * */
public class TFLSiteScraper implements TrainScraper {
    private Logger LOG = Logger.getLogger(TFLSiteScraper.class);
    private final TagSoupParser parser;
    private final ParserCache cache;

    public TFLSiteScraper() {
        this.parser = new TagSoupParser();
        this.cache = new ParserCache();
    }

    protected URL buildUrl(BranchStop branchStop, Branch branch) throws ParseException {
        TflStationCode tflStationCode = branchStop.getStationCode();
        String urlString = "http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=" + branch.getLine() + "&StationCode=" + tflStationCode.getCode() + "&Go=Go&switch=on";
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            LOG.warn("didn't get url: '" + urlString + "'", e);
            throw new ParseException("didn't get url: '" + urlString + "'", e);
        }
    }

    /**
     * won't return null
     */
    public BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException {
       return this.parser.parse(buildUrl(branchStop, branch));
    }
}
