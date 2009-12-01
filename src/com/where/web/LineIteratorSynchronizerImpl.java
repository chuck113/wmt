package com.where.web;

import com.where.domain.alg.LineIterator;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Charles Kubicek
 */
public class LineIteratorSynchronizerImpl implements LineIteratorSynchronizer {

    private final LineIterator lineIterator;

    public LineIteratorSynchronizerImpl(LineIterator lineIterator) {
        this.lineIterator = lineIterator;
    }

    public String getLine(String line){
        return doSynchronizedCachedResultParse(line);
    }

    private final Logger LOG = Logger.getLogger(LineIteratorSynchronizerImpl.class);

    // conccurrency objects
    private static final Map<String, LineParseResult> RESULTS = new ConcurrentHashMap<String, LineParseResult>();
    private static final Map<String, Object> LINES_MUTEXES = new ConcurrentHashMap<String, Object>();

    static {
        for(String line : WmtProperties.LINES_TO_ITERATE){
            RESULTS.put(line, new LineParseResult());
            LINES_MUTEXES.put(line, new Object());
        }
    }


    private void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException("rethrowing" + e);
        }
    }

    /**
     * This class holds a cache of recently parsed points as json that is retunred. The points
     * are valid for a certain amount of time. If the points are valid they are returned,
     * if they are not valid we do a parse and updated the cached points. If another
     * thread comes along while the parse is happening, the current, invalid points
     * are returned. If on the first parse another thread comes along it waits for
     * the parse to finish before returning.
     * <p/>
     * Returning the invalid result is simpler than waiting for the existing parse to
     * finish as there is less moving parts and the client gets results faster but
     * the client also will see out of date data for longer.
     */
    private String doSynchronizedCachedResultParse(String lineName) {
        LineParseResult resultObj = RESULTS.get(lineName);

        if (resultObj.isValid()) {
            safeSleep(3000);
            return resultObj.getResult().toString();
        } else {
            LOG.info("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " result is " + resultObj.getResult() + " at " + new Date());
            if (resultObj.getResult() == null) {//this will happen on the first parse before a result is created
                LOG.info("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " no result, entering mutex at " + new Date());
                synchronized (LINES_MUTEXES.get(lineName)) {
                    if (resultObj.getResult() == null) {
                        resultObj.setParseInProgress(true);
                        System.out.println("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " entered mutex and parsing...");
                        String resultJson = ResultTransformer.toJson(lineIterator.run(lineName));
                        resultObj.update(System.currentTimeMillis(), resultJson, false);
                        LOG.info("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " exiting mutex after running alg at " + new Date());
                        return resultJson.toString();
                    } else {
                        LOG.info("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " exiting mutex at " + new Date());
                        return resultObj.toString();
                    }
                }
            } else if (resultObj.isParseInProgress()) {
                LOG.info("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " json no longer valid, returning old result");
                // return the invalid result
                safeSleep(3000);
                return resultObj.getResult().toString();
            } else {
                LOG.info("AbstractLinesResource.represent " + lineName + " " + Thread.currentThread().getName() + " json no longer valid, reparsing...");
                resultObj.setParseInProgress(true);
                String resultJson = ResultTransformer.toJson(lineIterator.run(lineName));
                //FIXME there is no synchronization when udpating the object, should there be?
                resultObj.update(System.currentTimeMillis(), resultJson, false);
                return resultJson.toString();
            }
        }
    }
}
