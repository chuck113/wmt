package com.where.domain.alg;

import com.where.dao.hsqldb.TimeInfo;
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
public class Algorithm {

    private Logger LOG = Logger.getLogger(Algorithm.class);

    private final String branch;
    private final DaoFactory daoFactory;
    private final TrainScraper scraper;
    private final BoardParsing boardParsing;

    static private final int SCRAPER_RETRIES = 3;

    /**
     * If a severe failure occours while the alogrithm is running, this variable is set
     * to instruct the algorithm to stop and return as soon as possible
     */
    private LogicalParsingFailure lastSevereFailure;

    /**
     * If a failure occours while iterating a branch this variable is set to instruct
     * the algorithm to stop and move on to the next branch at the earliest opportunity
     */
    private LogicalParsingFailure lastBrnachFailure;


    // the directions in which we traverse the line, does not coralate to the board directions we read.
    //@@universal_traversal_directions = [Universal_direction.One, Universal_direction.Two]

    public Algorithm(String branch, DaoFactory daoFactory, TrainScraper scraper) {
        this.branch = branch;
        this.scraper = scraper;
        this.boardParsing = new BoardParsing(daoFactory);
        this.daoFactory = daoFactory;
    }


    public LinkedHashMap<AbstractDirection, List<Point>> run() {
        //Branch branch = dataMapper.getBranchNamesToBranches().get(this.branch);
        Branch branch = daoFactory.getBranchDao().getBranch(this.branch);
        //Set<Point> result = new HashSet<Point>();
        LinkedHashMap<AbstractDirection, List<Point>> result = new LinkedHashMap<AbstractDirection, List<Point>>();
        List<AbstractDirection> abstractDirections = Arrays.asList(AbstractDirection.values());

        for (AbstractDirection direction : abstractDirections) {
            LOG.info("begining pase for abstract direction " + direction.toString());
            if (lastSevereFailure == null) {
                result.put(direction, iterateForDirection(branch, direction));
            } else {
                LOG.info("stopping early due to sever error " + lastSevereFailure.getReason());
            }
        }

        return result;
    }

    private void setErrors(Algorithm.BoardData boardData) {
        if (hasError(boardData)) {
            LOG.warn("Failed parsing due to " + boardData.error.getReason() + ", instructed to " + boardData.error.getInstructions().toString());
            AlogorithmInstructionAfterFailure instructionAfterFailure = boardData.error.getInstructions();
            if (instructionAfterFailure.equals(AlogorithmInstructionAfterFailure.START_NEXT_BRANCH)) {
                lastBrnachFailure = boardData.error;
            } else if (instructionAfterFailure.equals(AlogorithmInstructionAfterFailure.GIVEUP)) {
                lastSevereFailure = boardData.error;
            }
        }
    }

    private boolean hasError(Algorithm.BoardData boardData) {
        return boardData.error != null && !(boardData.error instanceof NoError);
    }

    private boolean validateOkToProceede() {
        return lastBrnachFailure == null && lastSevereFailure == null;
    }

    /**
     * Iterates over a given branch in a given direction.
     *
     * @param branch
     * @param direction
     * @return
     */
    List<Point> iterateForDirection(Branch branch, AbstractDirection direction) {
        List<BranchStop> branchStops = daoFactory.getBranchDao().getBranchStops(branch);
        DirectionalBranchStopIterator iterator = new DirectionalBranchStopIterator(branchStops, direction);

        int iterationCount = 0; // to stop infinite loops

        //List<DiscoveredTrain> discoveredPoints = new ArrayList<DiscoveredTrain>();
        ResultBuilder bulider = new ResultBuilder(branchStops);

        while (iterator.hasNext() && iterationCount++ < 10) {
            BranchStop currentStop = iterator.next();
            LOG.info("currentStop: " + currentStop.getStation().getName());

            Algorithm.BoardData boardData = findNextAvailableStop(branch, direction, currentStop, branchStops);
            setErrors(boardData);
            if (!validateOkToProceede()) return Collections.<Point>unmodifiableList(bulider.results());
            DiscoveredTrain furthestTrain = bulider.processBoardData(boardData, currentStop, direction);

            if (furthestTrain == null) {
                LOG.info("furthest train at station: was NULL, moving iterator on one");
                // try the next station to see if there are any trains there
                if (iterator.hasNext()) {
                    iterator.next();
                } else {
                    return Collections.<Point>unmodifiableList(bulider.results());
                }
            } else {
                LOG.info("furthest train at station: " + furthestTrain.getFurthestStation().getStation().getName());
                iterator.updateTo(furthestTrain.getFurthestStation());
            }
        }

        LOG.info("finished");
        return Collections.<Point>unmodifiableList(bulider.results());
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

        public DiscoveredTrain processBoardData(Algorithm.BoardData boardData, BranchStop currentStop, AbstractDirection direction) {
            List<TimeInfo> timeInfo = boardData.timeInfo;
            DiscoveredTrain lastTrainToBeAdded = null;

            for (TimeInfo info : timeInfo) {
                LOG.info("info = " + info);

                if (info.getInfo().length() > 0) {
                    DiscoveredTrain discoveredPoint = boardParsing.findPosition(
                            info.getInfo(), currentStop.getStation().getName(), boardData.concreteDirection);

                    /*  it's possible that for a station X, we find the last board entry
                       is 'at Station Y'. When we then go to station Y, the first entry
                       will be 'At Platfrom', and so that single train will be added twice,
                       so we check to see if that train already exists
                    */
                    if (discoveredPoint != null && !discoveredPoints.contains(discoveredPoint)
                            /*&& doesStationAppearAfter(discoveredPoint.getFurthestStation(),currentStop,direction)*/) {
                        discoveredPoints.add(discoveredPoint);
                        lastTrainToBeAdded = discoveredPoint;
                    }
                } else {
                    LOG.warn("got no time info from stop " + currentStop.getStation().getName() + " missing out for now");
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
        private boolean doesStationAppearAfter(BranchStop discovered,BranchStop currentInMainIteration, AbstractDirection direction) {
            DirectionalBranchStopIterator iterator = new DirectionalBranchStopIterator(branchStops, direction);

            if(discoveredPoints.size() == 0){
               iterator.updateTo(currentInMainIteration);
            } else {
                DiscoveredTrain furthestDiscovered = discoveredPoints.get(discoveredPoints.size() - 1);
                if(iterator.comesAfter(currentInMainIteration,furthestDiscovered.getFurthestStation())){
                   iterator.updateTo(furthestDiscovered.getFurthestStation());
                } else {
                    iterator.updateTo(currentInMainIteration);
                }
            }

            while(iterator.hasNext()){
                BranchStop next = iterator.next();
                System.out.println("Algorithm$ResultBuilder.doesStationAppearAfter comparing next: "+next.getStation().getName()+" and discovered: "+discovered.getStation().getName());
                if(discovered.equals(next)){
                    return true;
                }
            }

            LOG.info("given station '"+discovered.getStation().getName() +"' does not appear after the last station discovered: '"+currentInMainIteration.getStation().getName()+"'");
            return false;
        }

        public String dump(Algorithm.BoardData boardData) {
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

    public enum BoardParserResult {
        OK, UNAVAILABLE;
    }

    /**
     * Data found on a board
     */
    private class BoardData {
        final List<TimeInfo> timeInfo;
        final Direction concreteDirection;
        final BranchStop foundStop;
        final LogicalParsingFailure error;

        private BoardData(BranchStop foundStop, List<TimeInfo> timeInfo, Direction concreteDirection, LogicalParsingFailure error) {
            this.foundStop = foundStop;
            this.timeInfo = timeInfo;
            this.concreteDirection = concreteDirection;
            this.error = error;
        }

        private BoardData(LogicalParsingFailure error) {
            this(null, null, null, error);
        }


        private BoardData(BranchStop foundStop, List<TimeInfo> timeInfo, Direction concreteDirection) {
            this(foundStop, timeInfo, concreteDirection, new NoError());
        }

        boolean hasError() {
            return error != null;
        }

        public String dump() {
            StringBuilder builder = new StringBuilder();
            builder.append("direction: " + concreteDirection.getName() + "\n");
            builder.append("foundStop: " + foundStop.getStation().getName() + "\n");
            builder.append("error: " + error.getReason() + "\n");

            for (TimeInfo info : timeInfo) {
                builder.append("info: " + info.getInfo() + ", " + info.getTime() + "\n");
            }
            return builder.toString() + "\n";
        }
    }

    /**
     * Takes into account errors
     *
     * @param branch
     * @param direction
     * @param branchStop
     * @return
     */
    private BoardData findNextAvailableStop(
            Branch branch, AbstractDirection direction, BranchStop branchStop, List<BranchStop> branchStops) {

        com.where.tfl.grabber.BoardParserResult result = getNextStop(branch, branchStop);
        Map<String, List<TimeInfo>> boardData;

        switch (result.getResultCode()) {
            case UNAVAILABLE: {
                BranchStop nextToTry = findNextBranchStop(branchStop, branchStops, direction);
                LOG.warn("recieved " + result.getResultCode() + " from getNextStop for station " + branchStop.getStation().getName() + ", trying next station " + nextToTry.getStation().getName());
                return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextToTry, branchStops);
            }
            case PARSE_EXCEPTION: {
                BranchStop nextToTry = findNextBranchStop(branchStop, branchStops, direction);
                LOG.warn("recieved " + result.getResultCode() + " from getNextStop for station " + branchStop.getStation().getName() + ", trying next station " + nextToTry.getStation().getName());
                return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextToTry, branchStops);
            }
            case OK: {
                boardData = result.getBoardData();
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));

                if (concreteDirection == null) {
                    LOG.warn("didn't find data for direction " + direction + ", signaling end of branch");
                    return new BoardData(new EndOfBranchFailure());
                } else {
                    return new BoardData(branchStop, boardData.get(concreteDirection.getName()), concreteDirection);
                }
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Our weak exception that informs us that somthing has gone wrong with the parsing, will
     * happen when all the stops on a branch are unavailable
     */
    private interface LogicalParsingFailure {
        String getReason();

        AlogorithmInstructionAfterFailure getInstructions();
    }

    /**
     * If parsing returns LogicalParsingFailure then tell the algrithim what is should do next
     * <p/>
     * FIXME it shoudn't be upto other methods to decide if the algorithm should stop or not,
     * instead the algorithm should decide based on the type of error
     */
    private enum AlogorithmInstructionAfterFailure {
        CONTINUE, START_NEXT_BRANCH, GIVEUP
    }

    private class EndOfBranchFailure implements LogicalParsingFailure {
        public String getReason() {
            return "End of branch";
        }

        public AlogorithmInstructionAfterFailure getInstructions() {
            return AlogorithmInstructionAfterFailure.START_NEXT_BRANCH;
        }
    }

    private class NoError implements LogicalParsingFailure {
        public String getReason() {
            return "No Error";
        }

        public AlogorithmInstructionAfterFailure getInstructions() {
            return AlogorithmInstructionAfterFailure.CONTINUE;
        }
    }

    private class HttpTimeoutFailure implements LogicalParsingFailure {
        public String getReason() {
            return "HTTP timeout";
        }

        public AlogorithmInstructionAfterFailure getInstructions() {
            return AlogorithmInstructionAfterFailure.GIVEUP;
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
        com.where.tfl.grabber.BoardParserResult result = getNextStop(branch, branchStop);
        LOG.info("Algorithm.findNextAvailableStopAfterUnavailableRecursive result: " + result.getResultCode() + " for station " + branchStop.getBranch().getName());

        /**
         * the last suggestion was unavailable, go on to the next one. If the next one is null
         * it means we have got to the end of the branch so log the appropiate error
         */
        if (result.getResultCode().equals(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.UNAVAILABLE)) {
            BranchStop nextBranchStop = findNextBranchStop(branchStop, branchStops, direction);
            if (nextBranchStop == null) return new BoardData(null, null, null, new EndOfBranchFailure());
            return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextBranchStop, branchStops);
            /*
            * The last tfl grab failed because of a timeout
            */
        } else if (result.getResultCode().equals(com.where.tfl.grabber.BoardParserResult.BoardParserResultCode.PARSE_EXCEPTION)) {
            return new BoardData(null, null, null, new HttpTimeoutFailure());
            /**
             * we've found a branch stop
             */
        } else {
            Map<String, List<TimeInfo>> boardData = result.getBoardData();
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

    private BranchStop getLastStopOnBranch(List<BranchStop> branchStops) {
        return branchStops.get(branchStops.size() - 1);
        //!branchStops.get(branchStops.size()-1).equals(branchStop)
    }

    /**
     * The input branchstop can never be the last branch stop
     *
     * @return
     */
    private BranchStop findNextBranchStop(BranchStop branchStop, List<BranchStop> branchStops, AbstractDirection direction) {
        assert (!getLastStopOnBranch(branchStops).equals(branchStop));
        DirectionalBranchStopIterator iter = new DirectionalBranchStopIterator(branchStops, direction);
        iter.updateTo(branchStop);

        while (iter.hasNext()) {
            if (iter.next().equals(branchStop)) {
                BranchStop stop = iter.next();
                LOG.info("findNextBranchStop given stop " + branchStop.getStation().getName() + " found next stopo to be " + stop.getStation().getName());
                return stop;
            }
        }

        //throw new IllegalStateException("Did not find branch stop " + branchStop.getStation().getName() + " in list of branches");
        return null;
    }

    private com.where.tfl.grabber.BoardParserResult getNextStop(Branch branch, BranchStop branchStop) {

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


}