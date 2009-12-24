package com.where.tfl.grabber;

import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import com.where.domain.BranchStop;
import com.where.domain.Branch;
import com.where.domain.TflStationCode;

/**
 * @author Charles Kubicek
 */
public class RecordingTFLSiteScraper implements ArrivalBoardScraper {
    private Logger LOG = Logger.getLogger(TFLSiteScraper.class);
    private final RegexParser parser;
    private final ParserPersistenceCache cache;


    public static class DataRecordingConfig {
        private final String longevityId;
        private final String targetFolderName;

        public DataRecordingConfig(String longevityId, String targetFolderName) {
            this.longevityId = longevityId;
            this.targetFolderName = targetFolderName;
        }

        public String getTargetFolderName() {
            return targetFolderName;
        }

        public String getLongevityId() {
            return longevityId;
        }
    }

    /**
     * Just record html to file
     */
    public RecordingTFLSiteScraper() {
        this.parser = new RegexParser();
        this.cache = new ParserPersistenceCache();
    }

    /**
     *
     */
    public RecordingTFLSiteScraper(DataRecordingConfig config) {
        this.parser = new RegexParser();
        this.cache = new ParserPersistenceCache(config.getLongevityId(), config.targetFolderName);
    }


    protected URL buildUrl(BranchStop branchStop, Branch branch) throws ParseException {
        TflStationCode tflStationCode = branchStop.getTflStationCode();
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
    public BoardParserResultFromStation get(BranchStop branchStop, Branch branch) throws ParseException {
        try {
            URL url = buildUrl(branchStop, branch);
            LOG.info("parsing url: " + url);
            String rawHtml = IOUtils.toString(buildUrl(branchStop, branch).openStream());
            cache.add(rawHtml, branchStop.getStation().getName());
            return new BoardParserResultFromStation(parser.parse(url.openStream()), branchStop);
        } catch (IOException e) {
            throw new ParseException("error", e);
        }
    }
}
