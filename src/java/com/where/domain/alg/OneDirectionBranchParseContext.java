package com.where.domain.alg;

import com.where.domain.Branch;
import com.where.domain.BranchStop;
import com.where.domain.Direction;
import com.where.domain.Line;
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

    private final Branch branch;
    private final AbstractDirection direction;
    private final List<BranchStop> branchStops;
    private BranchStop lastFoundBranchStop;

    private final ArrivalBoardScraper scraper;
    
    public OneDirectionBranchParseContext(
            Branch branch,
            AbstractDirection direction,
            List<BranchStop> branchStops,
            ArrivalBoardScraper scraper) {
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
        BoardParserResultFromStation result = grabStop(branchStop);
        Map<String, Map<String, List<String>>> boardData;

        switch (result.getResultCode()) {
            case UNAVAILABLE: {
                return dealWithFailedParse(this.lastFoundBranchStop, result);
            }
            case PARSE_EXCEPTION: {
                return dealWithFailedParse(this.lastFoundBranchStop,result);
            }
            case OK: {
                boardData = result.getBoardDataWithPlatforms();
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));

                if (concreteDirection == null) {
                    return new StationArrivalData(BranchIterationFailures.NO_TRAINS_FOR_DIRECTION);
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
     * TODO refactor to make non-recursive and use iterator instead
     *
     * @return
     */
    private StationArrivalData findNextAvailableStopAfterUnavailableRecursive(BranchStop branchStop) {
        BoardParserResultFromStation result = grabStop(branchStop);
        LOG.info("OneDirectionBranchParseContext.findNextAvailableStopAfterUnavailableRecursive result: " + result.getResultCode() + " for station " + branchStop.getStation().getName());

        switch (result.getResultCode()) {
            case UNAVAILABLE: {
                BranchStop nextBranchStop = findNextBranchStop(branchStop, true);
                if (nextBranchStop == null)
                    return new StationArrivalData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
                else
                    return findNextAvailableStopAfterUnavailableRecursive(nextBranchStop);
            }case PARSE_EXCEPTION: {
                /*
                 * The last tfl grab failed because of a timeout
                 */
                return new StationArrivalData(BranchIterationFailures.HTTP_TIMEOUT_FAIULURE);
            }case OK: {
                /**
                 * we've found a branch stop
                 */
                Map<String, Map<String, List<String>>> boardData = result.getBoardDataWithPlatforms();
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));
                /*
                * Will get a null direction if the board doesn't have any trains for the current direction
                */
                if (concreteDirection == null) {
                    BranchStop nextBranchStop = findNextBranchStop(branchStop, false);
                    if (nextBranchStop == null) {
                        return new StationArrivalData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
                    } else {
                        return findNextAvailableStopAfterUnavailableRecursive(nextBranchStop);
                    }
                } else {
                    return new StationArrivalData(branchStop, boardData.get(concreteDirection.getName()), concreteDirection);
                }
            } default:{
                throw new IllegalArgumentException("Unkown state '"+result.getResultCode()+"'");
            }
        }
    }

    /**
     */
    private BranchStop findNextBranchStop(BranchStop branchStop, boolean skip) {
        DirectionalBranchStopIterator iter = DirectionalBranchStopIterator.FACTORY.forAlgorithm(branchStops, direction);
        iter.updateTo(branchStop);

        if (skip) {
            iter.updateMidway();
        }

        if (iter.hasNext()) {
            BranchStop res = iter.next();
            LOG.info("last stop was: " + branchStop.getStationName() + ", next stop will be: " + res.getStationName());
            return res;
        } else {
            return null;
        }
    }

    private BoardParserResultFromStation grabStop(BranchStop branchStop) {
        try {
            return scraper.get(branchStop, branch);
        } catch (ParseException e) {
            LOG.warn("failed to scrape after all attempts, bailing and will try next");
            return new BoardParserResultFromStation(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.PARSE_EXCEPTION, Collections.<String, Map<String, List<String>>>emptyMap(), branchStop);
        }
    }

    private StationArrivalData dealWithFailedParse(BranchStop branchStop, BoardParserResultFromStation result) {
        BranchStop nextToTry = findNextBranchStop(branchStop, false);
        if (nextToTry == null) {
            return new StationArrivalData(BranchIterationFailures.END_OF_BRANCH_FAIULURE);
        }
        LOG.warn("recieved " + result.getResultCode() + " from grabStop for station " + branchStop.getStation().getName() + ", trying next station " + nextToTry.getStation().getName());
        return findNextAvailableStopAfterUnavailableRecursive(nextToTry);
    }
}
