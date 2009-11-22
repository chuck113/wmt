package com.where.web;

import com.where.domain.alg.BranchIterator;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Charles Kubicek
 */
public class BranchIteratorSynchronizerImpl implements BranchIteratorSynchronizer{

    private final com.where.domain.alg.BranchIterator branchIterator;

    public BranchIteratorSynchronizerImpl(BranchIterator branchIterator) {
        this.branchIterator = branchIterator;
    }

    public String getBranch(String branch){
        return doSynchronizedCachedResultParse(branch);
    }

    private final Logger LOG = Logger.getLogger(BranchIteratorSynchronizerImpl.class);

    // conccurrency objects
    private static final Map<String, BranchParseResult> RESULTS = new ConcurrentHashMap<String, BranchParseResult>();
    private static final Map<String, Object> BRANCH_MUTEXES = new ConcurrentHashMap<String, Object>();

    static {
        for(String branch : WmtProperties.LINES_TO_ITERATE){
            RESULTS.put(branch, new BranchParseResult());
            BRANCH_MUTEXES.put(branch, new Object());
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
    private String doSynchronizedCachedResultParse(String branchName) {
        BranchParseResult resultObj = RESULTS.get(branchName);

        if (resultObj.isValid()) {
            safeSleep(3000);
            return resultObj.getResult().toString();
        } else {
            LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " result is " + resultObj.getResult() + " at " + new Date());
            if (resultObj.getResult() == null) {//this will happen on the first parse before a result is created
                LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " no result, entering mutex at " + new Date());
                synchronized (BRANCH_MUTEXES.get(branchName)) {
                    if (resultObj.getResult() == null) {
                        resultObj.setParseInProgress(true);
                        System.out.println("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " entered mutex and parsing...");
                        String resultJson = ResultTransformer.toJson(branchIterator.run(branchName));
                        resultObj.update(System.currentTimeMillis(), resultJson, false);
                        LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " exiting mutex after running alg at " + new Date());
                        return resultJson.toString();
                    } else {
                        LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " exiting mutex at " + new Date());
                        return resultObj.toString();
                    }
                }
            } else if (resultObj.isParseInProgress()) {
                LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " json no longer valid, returning old result");
                // return the invalid result
                safeSleep(3000);
                return resultObj.getResult().toString();
            } else {
                LOG.info("BranchesResource.represent " + branchName + " " + Thread.currentThread().getName() + " json no longer valid, reparsing...");
                resultObj.setParseInProgress(true);
                String resultJson = ResultTransformer.toJson(branchIterator.run(branchName));                                        
                //FIXME there is no synchronization when udpating the object, should there be?
                resultObj.update(System.currentTimeMillis(), resultJson, false);
                return resultJson.toString();
            }
        }
    }
}
