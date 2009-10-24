package com.where.domain.alg;

//import com.where.hibernate.Branch;
//import com.where.hibernate.BranchStop;

import com.where.tfl.grabber.*;
import com.where.domain.*;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The algorith starts at the station after the start of a branch (the station at the end)
 * doesn't need to show which trains are coming), goes to the other end, switches
 * direction then goes back to the station before the station started at
 */
public class BranchIterator {

    private Logger LOG = Logger.getLogger(BranchIterator.class);

    private final String branch;
    private final DaoFactory daoFactory;
    private final ArrivalBoardScraper scraper;
    private final BoardParsing boardParsing;

    public BranchIterator(String branch, DaoFactory daoFactory, ArrivalBoardScraper scraper) {
        this.branch = branch;
        this.scraper = scraper;
        this.boardParsing = new BoardParsing(daoFactory);
        this.daoFactory = daoFactory;
    }


    public LinkedHashMap<AbstractDirection, List<Point>> run() {
        Branch branch = daoFactory.getBranchDao().getBranch(this.branch);
        LinkedHashMap<AbstractDirection, List<Point>> result = new LinkedHashMap<AbstractDirection, List<Point>>();
        List<AbstractDirection> abstractDirections = Arrays.asList(AbstractDirection.values());

        for (AbstractDirection direction : abstractDirections) {
            LOG.info("begining single direction branch iteration for branch '"+branch.getName()+"' for direction " + direction);
            ParseBranchResult branchResult = iterateForDirection(branch, direction);
            result.put(direction, branchResult.foundPoints);

            if (branchResult.error.shouldGiveUp()) {
                LOG.info("stopping early due to sever error " + branchResult.error.getReason());
                break;
            }
        }

        return result;
    }

    private static class ParseBranchResult {

        public final List<Point> foundPoints;
        public final LogicalParsingFailure error;

        private ParseBranchResult(List<DiscoveredTrain> foundPoints, LogicalParsingFailure error) {
            this.foundPoints = Collections.<Point>unmodifiableList(foundPoints);
            this.error = error;
        }
    }

    /**
     * Iterates over a given branch in a given direction.
     *
     * @param branch
     * @param direction
     * @return
     */
    ParseBranchResult iterateForDirection(Branch branch, AbstractDirection direction) {
        List<BranchStop> branchStops = daoFactory.getBranchDao().getBranchStops(branch);
        DirectionalBranchStopIterator iterator = DirectionalBranchStopIterator.FACTORY.forAlgorithm(branchStops, direction);

        int iterationCount = 0; // to stop infinite loops
        ResultBuilder bulider = new ResultBuilder(branchStops);

        OneDirectionBranchParseContext parseContext = new OneDirectionBranchParseContext(branch, direction, branchStops, scraper);

        while (iterator.hasNext() && iterationCount++ < 15) {
            BoardData boardData = parseContext.findNextAvailableStop(iterator.next());
            if (boardData.error.shouldGiveUp() || boardData.error.shouldStartNextBranch()) {
                return new ParseBranchResult(bulider.results(), boardData.error);
            }

            DiscoveredTrain furthestTrain = bulider.processBoardData(boardData, parseContext);

            if (furthestTrain == null) {
                LOG.info("furthest train at station: was NULL, moving iterator on one");
                // try the next station to see if there are any trains there
                if (iterator.hasNext()) {
                    iterator.next();
                } else {
                    new ParseBranchResult(bulider.results(), BranchParseFailures.NO_ERROR);
                }
            } else {
                LOG.info("furthest train at station: " + furthestTrain.getFurthestStation().getStation().getName());
                iterator.setNext(furthestTrain.getFurthestStation());
            }
        }

        LOG.info("finished parsing '"+branch.getName()+"' for direction '"+direction+"'");
        return new ParseBranchResult(bulider.results(), BranchParseFailures.NO_ERROR);
    }


    /**
     * An object that collects the results of a branch parse
     */
    private class ResultBuilder {
        private final List<DiscoveredTrain> discoveredPoints = new ArrayList<DiscoveredTrain>();
        private final List<BranchStop> branchStops;

        private ResultBuilder(List<BranchStop> branchStops) {
            this.branchStops = branchStops;
        }

        public DiscoveredTrain processBoardData(BoardData boardData, OneDirectionBranchParseContext parseContext) {
            List<String> timeInfo = boardData.timeInfo;
            DiscoveredTrain lastTrainToBeAdded = null;

            for (String info : timeInfo) {
                LOG.info("info = " + info);

                if (info.length() > 0) {
                    DiscoveredTrain discoveredPoint = boardParsing.findPosition(
                            info,
                            parseContext.getLastBranchStop().getStation().getName(),
                            boardData.concreteDirection,
                            parseContext.getBranch());

                    /*  it's possible that for a station X, we find the last board entry
                       is 'at Station Y'. When we then go to station Y, the first entry
                       will be 'At Platfrom', and so that single train will be added twice,
                       so we check to see if that train already exists
                    */
                    if (discoveredPoint != null && !discoveredPoints.contains(discoveredPoint)) {
                        boolean b = doesStationAppearAfter(discoveredPoint.getFurthestStation(), discoveredPoints.size() == 0 ? parseContext.getLastBranchStop() : discoveredPoints.get(discoveredPoints.size() - 1).getFurthestStation(), parseContext.getDirection());
                        System.out.println("BranchIterator$ResultBuilder.processBoardData does '" + discoveredPoint.getFurthestStation().getStationName() + "' appear after: '" + (discoveredPoints.size() == 0 ? parseContext.getLastBranchStop() : discoveredPoints.get(discoveredPoints.size() - 1).getFurthestStation()).getStationName() + "': " + b);

                        /*&& doesStationAppearAfter(discoveredPoint.getFurthestStation(),currentStop,direction)*/
                        //if(b){
                        discoveredPoints.add(discoveredPoint);
                        lastTrainToBeAdded = discoveredPoint;
                        //}
                    }
                    }else{
                        LOG.warn("got no time info from stop " + parseContext.getLastBranchStop().getStation().getName() + " missing out for now");
                        //TODO cope with no info; estimate the position
                    }
                }

                if (lastTrainToBeAdded == null) {
                    LOG.warn(dump(boardData));
                }

                return lastTrainToBeAdded;
            }

            /**
             * For bakerloo line it actually says some trains are after a station traveling away from it!
             * looks like a defect but we need to ignore them.
             *
             * @return
             */

        private boolean doesStationAppearAfter(BranchStop discovered, BranchStop currentInMainIteration, AbstractDirection direction) {
            DirectionalBranchStopIterator iterator = DirectionalBranchStopIterator.FACTORY.all(branchStops, direction);


            return iterator.comesAfter(currentInMainIteration, discovered);
//            if (discoveredPoints.size() == 0) {
//                iterator.updateTo(currentInMainIteration);
//            } else {
//                DiscoveredTrain furthestDiscovered = discoveredPoints.get(discoveredPoints.size() - 1);
//                if (iterator.comesAfter(currentInMainIteration, furthestDiscovered.getFurthestStation())) {
//                    iterator.updateTo(furthestDiscovered.getFurthestStation());
//                } else {
//                    iterator.updateTo(currentInMainIteration);
//                }
//            }
//
//            while (iterator.hasNext()) {
//                BranchStop next = iterator.next();
//                System.out.println("BranchIterator$ResultBuilder.doesStationAppearAfter comparing next: " + next.getStation().getName() + " and discovered: " + discovered.getStation().getName());
//                if (discovered.equals(next)) {
//                    return true;
//                }
//            }
//
//            LOG.info("given station '" + discovered.getStation().getName() + "' does not appear after the last station discovered: '" + currentInMainIteration.getStation().getName() + "'");
//            return false;
        }

        public String dump(BoardData boardData) {
            StringBuilder builder = new StringBuilder("lastTrainToBeAdded is null, dumping data");
            builder.append("board data dump:\n" + boardData.dump() + "\n");
            builder.append("all existing trains:\n");
            for (DiscoveredTrain discoveredPoint : discoveredPoints) {
                builder.append("  discription: " + discoveredPoint.getDescription() + "\n");
                BranchStop stop = discoveredPoint.getFurthestStation();
                if (stop != null) {
                    builder.append("  furthestStation: " + stop.getStation().getName() + "\n");
                } else {
                    builder.append("  furthestStation: was null\n");
                }
            }

            return builder.toString();
        }

        public List<DiscoveredTrain> results() {
            return new ArrayList<DiscoveredTrain>(discoveredPoints);
        }

    }
}