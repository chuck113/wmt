package com.where.tfl.grabber;

import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Branch;

import java.net.URL;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class CachingTflScraper extends TFLSiteScraper {

    private final Logger LOG = Logger.getLogger(CachingTflScraper.class);

    private ParserCache cache;


    public CachingTflScraper(){
       super();
       cache = new ParserCache();
    }

    @Override
    public BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException {
        URL url = super.buildUrl(branchStop, branch);

        try{
        cache.add(IOUtils.toString(url.openStream()), branchStop.getStation().getName());
        }catch(IOException e){
           LOG.warn("Didn't add to cache due to "+e.getMessage()); 
        }

        return super.get(branchStop, branch);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
