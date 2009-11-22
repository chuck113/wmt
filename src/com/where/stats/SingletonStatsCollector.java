package com.where.stats;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;
import com.where.domain.alg.AbstractDirection;
import com.where.domain.alg.LogicalParsingFailure;
import com.where.domain.Point;

/**
 * Keep refernces to a current iteration
 *
 * @author Charles Kubicek
 */
public class SingletonStatsCollector {

    private final Logger LOG = Logger.getLogger(SingletonStatsCollector.class);

    private static final int BRANCH_HISTORY = 50;

    private static final Date loadedAt = new Date();
    private static final Date createdAt = new Date();

    private final Map<String, Deque<BranchIterationsStats>> allStats = new ConcurrentHashMap<String, Deque<BranchIterationsStats>>();
    private final Map<String, BranchIterationsStats> currentStats = new ConcurrentHashMap<String, BranchIterationsStats>();

    private final Multiset<String> branchShallowHits = HashMultiset.create();

    public void firstDirectionDone(String branch, LogicalParsingFailure error) {
        currentStats.get(branch).directionDone(error);
    }

    public void reset(){
        allStats.clear();
        currentStats.clear();
    }

    public class BranchIterationsStats {

        private final long startTime;
        private long firstDirTime=0;
        private long endTime;
        private List<TflGrabStats> stats = new ArrayList<TflGrabStats>();
        private LinkedHashMap<AbstractDirection, List<Point>> result;
        private String error;

        public BranchIterationsStats() {
            this.startTime = new Date().getTime();
        }

        public void directionDone(LogicalParsingFailure error) {
            if(this.firstDirTime > 0 )return;
            this.firstDirTime = new Date().getTime();
            this.error = error.getReason();
        }

        public void finishedAll(LinkedHashMap<AbstractDirection, List<Point>> result) {
            this.endTime = new Date().getTime();
            this.result = result;
        }

        public void addTflGrab(String stationName, boolean cacheHit, long totalTime) {
            stats.add(new TflGrabStats(stationName, cacheHit, totalTime));
        }

        public long totalTimeTook(){
            return endTime - startTime;
        }

        public long timeTookForFirstDir(){
            return firstDirTime - startTime;
        }

         public long timeTookForSecondDir(){
            return endTime - firstDirTime;
        }

        public String errorFromFirstIter(){
            return error;
        }

        public Calendar completionGmtCompletionTime(){
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.setTime(new Date(endTime));
            return cal;
        }

        public int getNumberOfTrainsFound() {
            int count = 0;
            for (List<Point> ps : result.values()) {
                count += ps.size();
            }
            return count;
        }

        public List<TflGrabStats> getStats() {
            return Collections.unmodifiableList(stats);
        }

        public int getCacheHits(){
            int count = 0;
            for (TflGrabStats stat : stats) {
                if(stat.cacheHit)count++;
            }
            return count;
        }
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getLoadedAt() {
        return loadedAt;
    }

    private static final SingletonStatsCollector INSTANCE = new SingletonStatsCollector();

    public Map<String, Deque<BranchIterationsStats>> allStats(){
        return Collections.unmodifiableMap(allStats);
    }

    public static SingletonStatsCollector getInstance() {
        //return INSTANCE;
        return null;
    }

    private final class TflGrabStats {
        public final boolean cacheHit;
        public final long totalTime;
        public final String stationName;

        private TflGrabStats(String stationName, boolean cacheHit, long totalTime) {
            this.cacheHit = cacheHit;
            this.totalTime = totalTime;
            this.stationName = stationName;
        }
    }

    public void addTflGrab(String branch, String stationName, boolean cacheHit, long totalTime) {
        currentStats.get(branch).addTflGrab(stationName, cacheHit, totalTime);
    }

    public void shallowHit(String branch) {
        branchShallowHits.add(branch);
    }

    public int allShallowHits(){
        int count=0;
        for(String keys :branchShallowHits){
            count+= branchShallowHits.count(keys);
        }
        return count;
    }

    public void endIterating(String branch, LinkedHashMap<AbstractDirection, List<Point>> result) {
        currentStats.get(branch).finishedAll(result);
        SingletonStatsCollector.BranchIterationsStats iterationsStats = currentStats.remove(branch);
        if(!allStats.containsKey(branch)){
            allStats.put(branch, new ArrayDeque<BranchIterationsStats>(BRANCH_HISTORY));
        }

        Deque<BranchIterationsStats> stats = allStats.get(branch);
        if(stats.size() >= BRANCH_HISTORY){
            stats.removeLast();
        }
        stats.addFirst(iterationsStats);
    }

    public void startIterating(String branch) {
        if (currentStats.containsKey(branch)) {
            LOG.error("already parsing branch '" + branch + "'");
            return;
        }

        currentStats.put(branch, new BranchIterationsStats());
    }
}
