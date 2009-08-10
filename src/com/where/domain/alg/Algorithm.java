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

    public Algorithm(String branch, DaoFactory daoFactory, TrainScraper scraper ) {
        this.branch = branch;
        this.scraper = scraper;
        this.boardParsing = new BoardParsing(daoFactory);
        this.daoFactory = daoFactory;
    }


    public Set<Point> run() {
        //Branch branch = dataMapper.getBranchNamesToBranches().get(this.branch);
        Branch branch = daoFactory.getBranchDao().getBranch(this.branch);
        Set<Point> result = new HashSet<Point>();
        List<AbstractDirection> abstractDirections = Arrays.asList(AbstractDirection.values());

        for (AbstractDirection direction : abstractDirections) {
            LOG.info("begining pase for abstract direction "+direction.toString());
            if(lastSevereFailure == null)
                result.addAll(iterateForDirection(branch, direction));
            else {
                LOG.info("stopping early due to sever error "+lastSevereFailure.getReason());
            }
        }

        return result;
    }

    private void setErrors(Algorithm.BoardData boardData){
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

    private boolean hasError(Algorithm.BoardData boardData){
        return boardData.error != null && !(boardData.error instanceof NoError);
    }

    private boolean validateOkToProceede(){
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
        ResultBuilder bulider = new ResultBuilder();

        while (iterator.hasNext() && iterationCount++ < 10) {
            BranchStop currentStop = iterator.next();
            LOG.info("currentStop: " + currentStop.getStation().getName());

            Algorithm.BoardData boardData = findNextAvailableStop(branch, direction, currentStop, branchStops);
            setErrors(boardData);
            if(!validateOkToProceede()) return Collections.<Point>unmodifiableList(bulider.results());
            DiscoveredTrain furthestTrain = bulider.processBoardData(boardData, currentStop.getStation().getName());

            if(furthestTrain == null){
                LOG.info("furthest train at station: was NULL, moving iterator on one");
                // try the next station to see if there are any trains there
                if(iterator.hasNext()){
                    iterator.next();
                }else {
                    return Collections.<Point>unmodifiableList(bulider.results());
                }
            } else {
                LOG.info("furthest train at station: "+ furthestTrain.getFurthestStation().getStation().getName());
                iterator.updateTo(furthestTrain.getFurthestStation());
            }
        }

        LOG.info("finished");
        return Collections.<Point>unmodifiableList(bulider.results());
    }

    /**
     * An object that collects the results of a branch parse
     */
    private class ResultBuilder{
        private final Set<DiscoveredTrain> discoveredPoints = new HashSet<DiscoveredTrain>();

        public DiscoveredTrain processBoardData(Algorithm.BoardData boardData, String atStationName){
            List<TimeInfo> timeInfo = boardData.timeInfo;
            DiscoveredTrain lastTrainToBeAdded = null;

            for (TimeInfo info : timeInfo) {
                LOG.info("info = " + info);

                if (info.getInfo().length() > 0) {                    
                    DiscoveredTrain discoveredPoint = boardParsing.findPosition(
                            info.getInfo(), atStationName, boardData.concreteDirection);
                    
                    /* it's possible that for a station X, we find the last board entry
                        is 'at Station Y'. When we then go to station Y, the first entry
                        will be 'At Platfrom', and so that single train will be added twice,
                        so we check to see if that train already exists
                     */
                    if (discoveredPoint != null && !discoveredPoints.contains(discoveredPoint)){
                        discoveredPoints.add(discoveredPoint);
                        lastTrainToBeAdded = discoveredPoint;
                    }
                } else {
                    LOG.warn("got no time info from stop " + atStationName+" missing out for now");
                    //TODO cope with no info; estimate the position
                }
            }

            if(lastTrainToBeAdded == null){
                LOG.warn(dump(boardData));
            }

            return lastTrainToBeAdded;
        }

        public String dump(Algorithm.BoardData boardData){
            StringBuilder builder = new StringBuilder("lastTrainToBeAdded is null, dumping data");
                builder.append("board data dump:\n"+boardData.dump()+"\n");
                builder.append("all existing trains:\n");
                for (DiscoveredTrain discoveredPoint : discoveredPoints) {
                    builder.append("  discription: "+discoveredPoint.getDescription()+"\n");
                    BranchStop stop = discoveredPoint.getFurthestStation();
                    if(stop != null){
                       builder.append("  furthestStation: "+stop.getStation().getName()+"\n");
                    } else {
                        builder.append("  furthestStation: was null\n");
                    }
                }

            return builder.toString();
        }

        public List<DiscoveredTrain> results(){
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

        public String dump(){
            StringBuilder builder = new StringBuilder();
            builder.append("direction: "+concreteDirection.getName()+"\n");
            builder.append("foundStop: "+foundStop.getStation().getName()+"\n");
            builder.append("error: "+error.getReason()+"\n");

            for(TimeInfo info : timeInfo){
                builder.append("info: "+info.getInfo() +", "+info.getTime()+"\n");
            }
            return builder.toString()+"\n";
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
                LOG.warn("recieved "+result.getResultCode()+" from getNextStop for station "+branchStop.getStation().getName()+", trying next station "+nextToTry.getStation().getName());
                return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextToTry, branchStops);
            }
            case PARSE_EXCEPTION: {
                BranchStop nextToTry = findNextBranchStop(branchStop, branchStops, direction);
                LOG.warn("recieved "+result.getResultCode()+" from getNextStop for station "+branchStop.getStation().getName()+", trying next station "+nextToTry.getStation().getName());                
                return findNextAvailableStopAfterUnavailableRecursive(branch, direction, nextToTry, branchStops);
            }
            case OK: {
                boardData = result.getBoardData();
                Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));

                if(concreteDirection == null){
                    LOG.warn("didn't find data for direction "+direction+", signaling end of branch");
                    return new BoardData(new EndOfBranchFailure());
                } else{
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

    private BoardData findNextAvailableStopAfterUnavailableRecursive(Branch branch, AbstractDirection direction, BranchStop branchStop, List<BranchStop> branchStops) {
    // put back when iterator is finished
//        if (getLastStopOnBranch(branchStops).equals(branchStop)) {
//            //WRONG - we don't actually know which way we are iterating through this list.
//            return new BoardData(null, null, null, new EndOfBranchFailure());
//        }

        com.where.tfl.grabber.BoardParserResult result = getNextStop(branch, branchStop);
        System.out.println("Algorithm.findNextAvailableStopAfterUnavailableRecursive result: "+result.getResultCode() +" for station "+branchStop.getBranch().getName());
        if (result.getResultCode().equals(TagSoupParser.BoardParserResultCode.UNAVAILABLE)) {
            return findNextAvailableStopAfterUnavailableRecursive(branch, direction, findNextBranchStop(branchStop, branchStops, direction), branchStops);
        } else if (result.getResultCode().equals(TagSoupParser.BoardParserResultCode.PARSE_EXCEPTION)) {
            return new BoardData(null, null, null, new HttpTimeoutFailure());
        } else { // we've found a branch stop
            Map<String, List<TimeInfo>> boardData = result.getBoardData();
            Direction concreteDirection = direction.getConcreteDirection(new ArrayList<String>(boardData.keySet()));
            return new BoardData(branchStop, boardData.get(concreteDirection.getName()), concreteDirection);
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
                LOG.info("findNextBranchStop given stop "+branchStop.getStation().getName()+" found next stopo to be "+stop.getStation().getName());
                return stop;
            }
        }

        throw new IllegalStateException("Did not find branch stop " + branchStop.getStation().getName() + " in list of branches");
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
                    return new com.where.tfl.grabber.BoardParserResult(TagSoupParser.BoardParserResultCode.PARSE_EXCEPTION, Collections.EMPTY_MAP);
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