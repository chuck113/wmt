package com.where.domain.alg;

import com.where.tfl.grabber.ArrivalBoardScraper;
import com.where.tfl.grabber.BoardParserResult;
import com.where.tfl.grabber.ParseException;
import com.where.tfl.grabber.BoardParserResultFromStation;
import com.where.domain.BranchStop;
import com.where.domain.Branch;
import com.where.stats.SingletonStatsCollector;
import com.google.common.collect.*;

import java.util.*;

/**
 * Idea is to save bandwidth by using the results of the first directional parse in the second
 * parse.
 *
 * @author Charles Kubicek
 */
public class ReusingArrivalBoardScraper implements ArrivalBoardScraper {
    private boolean switchedDirection = false;
    private boolean done = false;
    private final ArrivalBoardScraper delegateScraper;
    private final LinkedHashMap<BranchStop, BoardParserResultFromStation> gatheredCache = new LinkedHashMap<BranchStop, BoardParserResultFromStation>();
    private List<BranchStop> queryCacheKeys;
    private List<BranchStop> branchStops;

    private BranchStop lastSecondIterationStopReturned = null;

    //TODO
    private static interface ScrapingStrategy extends ArrivalBoardScraper {
    }

    public BoardParserResultFromStation get(BranchStop branchStop, Branch branch) throws ParseException {
        if (done) {
            throw new IllegalStateException("can only switch direction once");
        } else if (switchedDirection) {
            BoardParserResultFromStation result = getWithReuse(branchStop, branch, lastSecondIterationStopReturned);
            lastSecondIterationStopReturned = result.getFromStation();
            return result;
        } else {
            long start = new Date().getTime();
            BoardParserResultFromStation res = delegateScraper.get(branchStop, branch);
            if (res.getResultCode() == BoardParserResult.BoardParserResultCode.OK) {
                gatheredCache.put(branchStop, res);
            }
            //SingletonStatsCollector.getInstance().addTflGrab(branch.getName(), branchStop.getStationName(), false, (new Date().getTime() - start));
            return res;
        }
    }

    /**
     * DONT' TOUCH!
     */
    private BoardParserResultFromStation getWithReuse(BranchStop branchStop, Branch branch, BranchStop lastStopReturned) throws ParseException {
        if (queryCacheKeys.contains(branchStop)) {
            //SingletonStatsCollector.getInstance().addTflGrab(branch.getName(), branchStop.getStationName(), true, 0);

            return gatheredCache.get(branchStop);
        } else {
            long start = new Date().getTime();
            // find a station in the cache that will have results we would've found for the given
            // branch stop, this will be the closest branchStop behind the given branch stop.
            // To find that, iterate through the cache until we find a stop/results that comes after the query,
            // then use the cache entry before.
            DirectionalBranchStopIterator iterator = DirectionalBranchStopIterator.FACTORY.all(branchStops, AbstractDirection.TWO);
            BranchStop foundOnLastIteration = null;

//          //TODO binary search here
            for (BranchStop cacheStop : queryCacheKeys) {
                if (iterator.comesBefore(cacheStop, branchStop)) {
                    break;
                } else {
                    foundOnLastIteration = cacheStop;
                }
            }

            if (foundOnLastIteration == null) {
                //SingletonStatsCollector.getInstance().addTflGrab(branch.getName(), branchStop.getStationName(), false, (new Date().getTime() - start));
                return delegateScraper.get(branchStop, branch);
            } else {
                // if the station found is the same as or appears before the last station we can't use it
                // because it's results wont' contain any new trains since the last scrape. E.G for victoria
                // 2nd iteration going south:
                // finished using Highbury & Islington in place of station to scrape Kings Cross
                // - Need to get scrape for Green Park
                // - Find Oxford Circus in queryCacheKeys, furthest station is Vauxhall
                // - Need to get scrape for Vauxhall, best reuse station was oxford circus
                // - can't use oxford circus because it comes before Green Park.
               if(lastStopReturned.equals(foundOnLastIteration)){
                    //SingletonStatsCollector.getInstance().addTflGrab(branch.getName(), branchStop.getStationName(), false, (new Date().getTime() - start));
                    return delegateScraper.get(branchStop, branch);
                } else if(iterator.comesAfter(lastStopReturned, branchStop)){
                    //SingletonStatsCollector.getInstance().addTflGrab(branch.getName(), branchStop.getStationName(), false, (new Date().getTime() - start));
                    return delegateScraper.get(branchStop, branch);
                } else{
                   // SingletonStatsCollector.getInstance().addTflGrab(branch.getName(), branchStop.getStationName(), true, (new Date().getTime() - start));
                    BoardParserResultFromStation foundRes = gatheredCache.get(foundOnLastIteration);
                    return new BoardParserResultFromStation(
                            foundRes.getResultCode(),
                            foundRes.getBoardDataWithPlatforms(),
                            foundOnLastIteration);
                }
            }
        }
    }

    public ReusingArrivalBoardScraper(ArrivalBoardScraper delegateScraper, List<BranchStop> branchStops) {
        this.delegateScraper = delegateScraper;
        this.branchStops = branchStops;
    }

    public void directionDone() {
        if (switchedDirection) {
            done = true;
        } else {
            switchedDirection = true;
            //iterator = DirectionalBranchStopIterator.FACTORY.all(branchStops, AbstractDirection.TWO);
            ImmutableList.copyOf(gatheredCache.values());
            queryCacheKeys = ImmutableList.copyOf(Iterables.reverse(ImmutableList.copyOf(gatheredCache.keySet())));
        }
    }
}
