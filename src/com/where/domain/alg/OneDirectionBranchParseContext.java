package com.where.domain.alg;

import com.where.domain.Branch;
import com.where.domain.BranchStop;
import com.where.domain.Direction;
import com.where.tfl.grabber.ParseException;
import com.where.tfl.grabber.ArrivalBoardScraper;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 *         <p/>
 *         Keeps objects and methods relevant for one branch iteration
 */
public class OneDirectionBranchParseContext {

    private final Logger LOG = Logger.getLogger(OneDirectionBranchParseContext.class);

    static private final int SCRAPER_RETRIES = 3;

    private final Branch branch;
    private final AbstractDirection direction;
    private final List<BranchStop> branchStops;
    private BranchStop branchStop; // rename

    private final ArrivalBoardScraper scraper;
    //private final BoardParsing boardParsing;

    public OneDirectionBranchParseContext(Branch branch, AbstractDirection direction, List<BranchStop> branchStops, ArrivalBoardScraper scraper) {
        this.branch = branch;
        this.direction = direction;
        this.branchStops = branchStops;
        this.scraper = scraper;
    }

    public BranchStop getLastBranchStop() {
        return branchStop;
    }

    public void setLastBranchStop(BranchStop lastBranchStop) {
        this.branchStop = lastBranchStop;
    }

    public List<BranchStop> getBranchStops() {
        return branchStops;
    }

    public AbstractDirection getDirection() {
        return direction;
    }

    public Branch getBranch() {
        return branch;
    }

    /**
     * Takes into account errors
     *
     * @return
     * @param branchStop
     */
    public BoardData findNextAvailableStop(BranchStop branchStop) {
        LOG.info("currentStop is now: " + branchStop.getStation().getName());
        this.branchStop = branchStop;
        com.where.tfl.grabber.BoardParserResult result = grabStop(branch, this.branchStop);
        Map<String, List<String>> boardData;

        switch (result.getResultCode()) {
            case UNAVAILABLE: {
                return dealWithFailedParse(branch, direction, this.branchStop, branchStops, result);
            }
            case PARSE_EXCEPTION: {
                return dealWithFailedParse(branch, direction, this.branchStop, branchStops, result);
            }
            case OK: {
                boardData = result.getBoardData();
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));

                if (concreteDirection == null) {
                    LOG.warn("didn't find data for direction " + direction + ", signaling end of branch");
                    return new BoardData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
                } else {
                    return new BoardData(this.branchStop, boardData.get(concreteDirection.getName()), concreteDirection);
                }
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Try to find the next stop when a stop was unavailable - essentially recover
     * a branch parse after a failure. The whole branch could be out of action.
     *
     * @param branch
     * @param direction
     * @param branchStop
     * @param branchStops
     * @return
     */
    private BoardData findNextAvailableStopAfterUnavailableRecursive(Branch branch, AbstractDirection direction, BranchStop branchStop, List<BranchStop> branchStops) {
        com.where.tfl.grabber.BoardParserResult result = grabStop(branch, branchStop);
        LOG.info("BranchIterator.findNextAvailableStopAfterUnavailableRecursive result: " + result.getResultCode() + " for station " + branchStop.getStation().getName());

        /**
         * the last suggestion was unavailable, go on to the next one. If the next one is null
         * it means we have got to the end of the branch so log the appropiate error
         */
        if (result.getResultCode().equals(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.UNAVAILABLE)) {
            BranchStop nextBranchStop = findNextBranchStop(branchStop, branchStops, direction);
            if (nextBranchStop == null)
                return new BoardData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
            return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextBranchStop, branchStops);
            /*
            * The last tfl grab failed because of a timeout
            */
        } else if (result.getResultCode().equals(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.PARSE_EXCEPTION)) {
            return new BoardData(BranchIterationFailures.HTTP_TIMEOUT_FAIULURE);
            /**
             * we've found a branch stop
             */
        } else {
            Map<String, List<String>> boardData = result.getBoardData();
            Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));
            /*
             * Will get a null direction if the board doesn't have any trains for the current direction
             */
            if (concreteDirection == null) {
                return findNextAvailableStopAfterUnavailableRecursive(branch, direction, findNextBranchStop(branchStop, branchStops, direction), branchStops);
            } else {
                return new BoardData(branchStop, boardData.get(concreteDirection.getName()), concreteDirection);
            }
        }
    }

    /**
     * The input branchstop can never be the last branch stop
     *
     * @return
     */
    private BranchStop findNextBranchStop(BranchStop branchStop, List<BranchStop> branchStops, AbstractDirection direction) {
        DirectionalBranchStopIterator iter = DirectionalBranchStopIterator.FACTORY.forAlgorithm(branchStops, direction);
        iter.updateTo(branchStop);

        if (iter.hasNext()) {
            BranchStop res = iter.next();
            LOG.info("last stop was: " + branchStop + ", next stop will be: " + res);
            return res;
        } else {
            return null;
        }
    }

    private com.where.tfl.grabber.BoardParserResult grabStop(Branch branch, BranchStop branchStop) {
        int attempts = 0;

        while (attempts < SCRAPER_RETRIES) {

            try {
                return scraper.get(branchStop, branch);
            } catch (ParseException e) {
                attempts++;
                LOG.warn("failed to scrape, attempt no " + attempts, e);
                if (attempts == SCRAPER_RETRIES) {
                    LOG.warn("failed to scrape after all attempts, bailing");
                    return new com.where.tfl.grabber.BoardParserResult(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.PARSE_EXCEPTION, Collections.EMPTY_MAP);
                }

                try {
                    Thread.sleep(3000 * attempts);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
        // should never get here...
        throw new IllegalStateException("should never get here");
    }

    private BoardData dealWithFailedParse(Branch branch, AbstractDirection direction, BranchStop branchStop, List<BranchStop> branchStops, com.where.tfl.grabber.BoardParserResult result) {
        BranchStop nextToTry = findNextBranchStop(branchStop, branchStops, direction);
        if (nextToTry == null) {
            return new BoardData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
        }
        LOG.warn("recieved " + result.getResultCode() + " from grabStop for station " + branchStop.getStation().getName() + ", trying next station " + nextToTry.getStation().getName());
        return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextToTry, branchStops);
    }
}
