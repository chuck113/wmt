package com.where.domain.alg;

import com.where.domain.Branch;
import com.where.domain.BranchStop;
import com.where.domain.Direction;
import com.where.tfl.grabber.ParseException;
import com.where.tfl.grabber.ArrivalBoardScraper;
import com.where.tfl.grabber.BoardParserResultFromStation;

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
    private BranchStop lastFoundBranchStop;

    private final ArrivalBoardScraper scraper;
    //private final BoardParsing boardParsing;

    public OneDirectionBranchParseContext(Branch branch, AbstractDirection direction, List<BranchStop> branchStops, ArrivalBoardScraper scraper) {
        this.branch = branch;
        this.direction = direction;
        this.branchStops = branchStops;
        this.scraper = scraper;
    }

    public BranchStop getLastBranchStop() {
        return lastFoundBranchStop;
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
     * @param branchStop
     * @return
     */
    public StationArrivalData findNextAvailableStop(BranchStop branchStop) {
        LOG.info("currentStop is now: " + branchStop.getStation().getName());
        this.lastFoundBranchStop = branchStop;
        BoardParserResultFromStation result = grabStop(branch, branchStop);
        Map<String, Map<String, List<String>>> boardData;

        if (result == null) {
            System.out.println("OneDirectionBranchParseContext.findNextAvailableStop resutl is null");
        }

        switch (result.getResultCode()) {
            case UNAVAILABLE: {
                return dealWithFailedParse(branch, direction, this.lastFoundBranchStop, branchStops, result);
            }
            case PARSE_EXCEPTION: {
                return dealWithFailedParse(branch, direction, this.lastFoundBranchStop, branchStops, result);
            }
            case OK: {
                boardData = result.getBoardDataWithPlatforms();
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));

                if (concreteDirection == null) {
                    LOG.warn("didn't find data for direction " + direction + ", signaling end of branch");
                    return new StationArrivalData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
                } else {
                    return new StationArrivalData(result.getFromStation(), boardData.get(concreteDirection.getName()), concreteDirection);
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
     * @return
     */
    private StationArrivalData findNextAvailableStopAfterUnavailableRecursive(Branch branch, AbstractDirection direction, BranchStop branchStop, List<BranchStop> branchStops) {
        BoardParserResultFromStation result = grabStop(branch, branchStop);
        LOG.info("BranchIterator.findNextAvailableStopAfterUnavailableRecursive result: " + result.getResultCode() + " for station " + branchStop.getStation().getName());

        /**
         * the last suggestion was unavailable, go on to the next one. If the next one is null
         * it means we have got to the end of the branch so log the appropiate error
         */
        if (result.getResultCode().equals(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.UNAVAILABLE)) {
            BranchStop nextBranchStop = findNextBranchStop(branchStop, branchStops, direction);
            if (nextBranchStop == null)
                return new StationArrivalData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
            return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextBranchStop, branchStops);
            /*
            * The last tfl grab failed because of a timeout
            */
        } else if (result.getResultCode().equals(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.PARSE_EXCEPTION)) {
            return new StationArrivalData(BranchIterationFailures.HTTP_TIMEOUT_FAIULURE);
            /**
             * we've found a branch stop
             */
        } else {
            Map<String, Map<String, List<String>>> boardData = result.getBoardDataWithPlatforms();
            Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));
            /*
             * Will get a null direction if the board doesn't have any trains for the current direction
             */
            if (concreteDirection == null) {
                return findNextAvailableStopAfterUnavailableRecursive(branch, direction, findNextBranchStop(branchStop, branchStops, direction), branchStops);
            } else {
                return new StationArrivalData(branchStop, boardData.get(concreteDirection.getName()), concreteDirection);
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
            LOG.info("last stop was: " + branchStop.getStationName() + ", next stop will be: " + res.getStationName());
            return res;
        } else {
            return null;
        }
    }

    private BoardParserResultFromStation grabStop(Branch branch, BranchStop branchStop) {
        try {
            return scraper.get(branchStop, branch);
        } catch (ParseException e) {
            LOG.warn("failed to scrape after all attempts, bailing and will try next");
            return new BoardParserResultFromStation(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.PARSE_EXCEPTION, Collections.EMPTY_MAP, branchStop);

        }
    }

    private StationArrivalData dealWithFailedParse(Branch branch, AbstractDirection direction, BranchStop branchStop, List<BranchStop> branchStops, BoardParserResultFromStation result) {
        BranchStop nextToTry = findNextBranchStop(branchStop, branchStops, direction);
        if (nextToTry == null) {
            return new StationArrivalData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
        }
        LOG.warn("recieved " + result.getResultCode() + " from grabStop for station " + branchStop.getStation().getName() + ", trying next station " + nextToTry.getStation().getName());
        return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextToTry, branchStops);
    }
}
