package com.where.tfl.grabber;

import com.where.domain.Branch;
import com.where.domain.BranchStop;
import com.where.domain.TflStationCode;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 *
 */
public class TFLSiteScraper implements ArrivalBoardScraper {
    private Logger LOG = Logger.getLogger(TFLSiteScraper.class);
    private final RegexParser parser = new RegexParser();

    protected URL buildUrl(BranchStop branchStop, Branch branch) throws ParseException {
        TflStationCode tflStationCode = branchStop.getTflStationCode();
        String urlString = "http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=" + branch.getLine() + "&StationCode=" + tflStationCode.getCode() + "&Go=Go&switch=on";
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new ParseException("didn't get url: '" + urlString + "'", e);
        }
    }

    /**
     * won't return null
     */
    public BoardParserResultFromStation get(BranchStop branchStop, Branch branch) throws ParseException {
        try {
            URL url = buildUrl(branchStop, branch);
            LOG.info("parsing url: "+url);
            return new BoardParserResultFromStation(parser.parse(url.openStream()), branchStop);
        } catch (IOException e) {
            throw new ParseException("error: '"+e.getMessage()+"'", e);
        }
    }
}
