package com.where.tfl.grabber;

import com.where.domain.BranchStop;
import com.where.domain.Branch;

import java.net.URL;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 */
public class CachingTflScraper extends TFLSiteScraper {

    private final Logger LOG = Logger.getLogger(CachingTflScraper.class);

    /**
     * equals/hashcode only use entrytime
     */
    private static final class CacheEntry{
        public final BoardParserResult result;
        public final long entryTime;

        private CacheEntry(BoardParserResult result, long entryTime) {
            this.result = result;
            this.entryTime = entryTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheEntry that = (CacheEntry) o;

            if (entryTime != that.entryTime) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result1 = (int) (entryTime ^ (entryTime >>> 32));
            return result1;
        }
    }

    private static final HashMap<String, CacheEntry> CACHE_ENTRIES = new HashMap<String, CacheEntry>();
    private static long CACHE_ENTRY_LIFETIME = 20 * 1000;

    public CachingTflScraper(){
       super();
    }

    @Override
    public BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException {
        String station = branchStop.getStation().getName();
        BoardParserResult result = findResultInCache(station);
        if(result != null){
            LOG.info("successfully got data for "+station+" from cache");            
            return result;
        }

        result = super.get(branchStop, branch);
        CACHE_ENTRIES.put(station, new CacheEntry(result, System.currentTimeMillis()));
        return result;
    }

    private BoardParserResult findResultInCache(String station){
        CacheEntry cacheEntry = CACHE_ENTRIES.get(station);

        if(cacheEntry != null){
            if(cacheEntry.entryTime + CACHE_ENTRY_LIFETIME > System.currentTimeMillis()){
                System.out.println("removing entry: "+cacheEntry.result);
                CACHE_ENTRIES.remove(cacheEntry);
            } else {
                return cacheEntry.result;
            }
        }
        return null;
    }
}
