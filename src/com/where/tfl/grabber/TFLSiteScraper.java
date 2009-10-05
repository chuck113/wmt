package com.where.tfl.grabber;

import com.where.domain.Branch;
import com.where.domain.BranchStop;
import com.where.domain.TflStationCode;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

/**
 * */
public class TFLSiteScraper implements TrainScraper {
    private Logger LOG = Logger.getLogger(TFLSiteScraper.class);
    private final TagSoupResultBuilderParser parser;
    private final TagSoupStationBoardHtmlParser htmlChuckParser;
    private final ParserPersistenceCache cache;
    private final Map<BranchStop, String> rawHtmlCache = new HashMap<BranchStop, String>();


    public TFLSiteScraper() {
        this.parser = new TagSoupResultBuilderParser();
        this.cache = new ParserPersistenceCache();
        this.htmlChuckParser = new TagSoupStationBoardHtmlParser();
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
    public BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException {
        try {
            URL url = buildUrl(branchStop, branch);
            LOG.info("parsing url: "+url);
            String rawHtml = IOUtils.toString(buildUrl(branchStop, branch).openStream());
            try{
                String html = htmlChuckParser.parse(rawHtml);
                if(html != null)rawHtmlCache.put(branchStop, html);
                LOG.info("TFLSiteScraper.get added baranch stop "+branchStop.getStation().getName()+" with id "+branchStop.hashCode());
            }catch (Exception e){
                LOG.warn("Didn't make html chuck due to '"+e.getMessage()+"', ignoring");
            }
            cache.add(rawHtml, branchStop.getStation().getName());
            return this.parser.parse(rawHtml);
        } catch (IOException e) {
            throw new ParseException("error", e);
        }
    }

    public Map<BranchStop, String> getHtmlTables(){
        return Collections.unmodifiableMap(rawHtmlCache);
    }
}
