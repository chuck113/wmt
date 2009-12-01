package com.where.domain.alg;

//import com.where.hibernate.Branch;
//import com.where.hibernate.BranchStop;

import com.where.tfl.grabber.*;
import com.where.domain.*;
import com.google.common.collect.*;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The algorith starts at the station after the start of a branch (the station at the end)
 * doesn't need to show which trains are coming), goes to the other end, switches
 * direction then goes back to the station before the station started at
 */
public class BranchIteratorImpl implements BranchIterator {

    private Logger LOG = Logger.getLogger(BranchIteratorImpl.class);

    private final DaoFactory daoFactory;
    private final ArrivalBoardScraper scraper;
    private final BoardParsing boardParsing;

    public BranchIteratorImpl(DaoFactory daoFactory, ArrivalBoardScraper scraper) {
        this.scraper = scraper;
        this.boardParsing = new BoardParsing(daoFactory);
        this.daoFactory = daoFactory;
    }


    public SetMultimap<AbstractDirection,Point> run(String branchName) {
        Branch branch = daoFactory.getBranchDao().getBranch(branchName);
        List<BranchStop> branchStops = daoFactory.getBranchDao().getBranchStops(branch);
        ReusingArrivalBoardScraper reusingScraper = new ReusingArrivalBoardScraper(scraper, branchStops);
        LinkedHashMultimap<AbstractDirection,Point> result = LinkedHashMultimap.create();
        List<AbstractDirection> abstractDirections = Arrays.asList(AbstractDirection.values());

        //SingletonStatsCollector.getInstance().startIterating(branchName);

        for (AbstractDirection direction : abstractDirections) {
            LOG.info("begining single direction branch iteration for branch '" + branch.getName() + "' for direction " + direction);
            ParseBranchResult branchResult = iterateForDirection(branch, direction, reusingScraper);
            result.putAll(direction, branchResult.foundPoints);

            reusingScraper.directionDone();
            //SingletonStatsCollector.getInstance().firstDirectionDone(branchName, branchResult.error);

            if (branchResult.error.shouldGiveUp()) {
                LOG.info("stopping early due to sever error " + branchResult.error.getReason());
                break;
            }
        }

        //SingletonStatsCollector.getInstance().endIterating(branchName, result);
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
    ParseBranchResult iterateForDirection(Branch branch, AbstractDirection direction, ArrivalBoardScraper scraper) {
        List<BranchStop> branchStops = daoFactory.getBranchDao().getBranchStops(branch);
        DirectionalBranchStopIterator iterator = DirectionalBranchStopIterator.FACTORY.forAlgorithm(branchStops, direction);

        int nullResultCount = 0;
        int maxNullResultCountBeforeQuiting = 3;
        ResultBuilder bulider = new ResultBuilder(branchStops);

        OneDirectionBranchParseContext parseContext = new OneDirectionBranchParseContext(branch, direction, branchStops, scraper);

        while (iterator.hasNext()) {
            System.out.println("BranchIteratorImpl.iterateForDirection getting station data for stop "+iterator.peek());
            StationArrivalData stationArrivalData = parseContext.findNextAvailableStop(iterator.next());
            if (stationArrivalData.error.shouldGiveUp() || stationArrivalData.error.shouldStartNextBranch()) {
                return new ParseBranchResult(bulider.results(), stationArrivalData.error);
            } else if(stationArrivalData.error.shouldSkipStops()){
                iterator.updateMidway();
            } else {
                DiscoveredTrain furthestTrain = bulider.processBoardData(stationArrivalData, parseContext);

                // if the furthest station is null it means the last scrape found trains but didn't
                // find any that were new to us so it's OK to carry on.
                if (furthestTrain == null) {
                    if(nullResultCount++ == maxNullResultCountBeforeQuiting){
                        LOG.info("bailing branch iterator as had 3 null trains");
                        return new ParseBranchResult(bulider.results(), BranchIterationFailures.NO_ERROR);
                    }
                    LOG.info("furthest train at station was NULL, moving iterator on one");
                    // try the next station to see if there are any trains there
                    if (iterator.hasNext()) {
                        iterator.next();
                    } else {
                        return new ParseBranchResult(bulider.results(), BranchIterationFailures.NO_ERROR);
                    }
                } else {
                    LOG.info("furthest train at station: " + furthestTrain.getFurthestStation().getStation().getName());
                    iterator.setNext(furthestTrain.getFurthestStation());
                }
            }
        }

        LOG.info("finishedAll parsing '" + branch.getName() + "' for direction '" + direction + "'");
        return new ParseBranchResult(bulider.results(), BranchIterationFailures.NO_ERROR);
    }


    /**
     * An object that collects the results of a branch parse
     */
    private class ResultBuilder {
        private final List<DiscoveredTrain> discoveredPoints = new ArrayList<DiscoveredTrain>(); //should be linkedHashSet
        private final List<BranchStop> branchStops;

        private ResultBuilder(List<BranchStop> branchStops) {
            this.branchStops = branchStops;
        }

        public DiscoveredTrain processBoardData(StationArrivalData stationArrivalData, OneDirectionBranchParseContext parseContext) {
            Map<String, List<DiscoveredTrain>> trainsAtPlatforms = buildDiscoveredTrains(stationArrivalData, parseContext);
            List<DiscoveredTrain> discoveredTrains = findPlatformWithSmallestRange(trainsAtPlatforms, parseContext.getDirection());
            DiscoveredTrain lastTrainToBeAdded = null;

            for (DiscoveredTrain discoveredPoint : discoveredTrains) {
                /*  it's possible that for a station X, we find the last board entry
                   is 'at Station Y'. When we then go to station Y, the first entry
                   will be 'At Platfrom', and so that single train will be added twice,
                   so we check to see if that train already exists
                */
                if (discoveredPoint != null && !discoveredPoints.contains(discoveredPoint)) {
                    BranchStop furthestKnownTrain = getLastDiscoveredBranchStop();
                    if (furthestKnownTrain == null) {
                        // this happens for the results of the first stop on a line
                        discoveredPoints.add(discoveredPoint);
                        lastTrainToBeAdded = discoveredPoint;
                    } else if (doesStationAppearBefore(discoveredPoint.getFurthestStation(), furthestKnownTrain, parseContext.getDirection())) {
                        LOG.info("didn't add '" + discoveredPoint.getFurthestStation().getStationName() + "' to results as it was not further away than '" + furthestKnownTrain.getStationName() + "'");
                    } else {
                        discoveredPoints.add(discoveredPoint);
                        lastTrainToBeAdded = discoveredPoint;
                    }
                }

            }

            if (lastTrainToBeAdded == null && LOG.isDebugEnabled()) {
                LOG.debug(dump(stationArrivalData));
            }

            return lastTrainToBeAdded;
        }

        private BranchStop getLastDiscoveredBranchStop() {
            return discoveredPoints.size() == 0 ? null : discoveredPoints.get(discoveredPoints.size() - 1).getFurthestStation();
        }

        private Map<String, List<DiscoveredTrain>> buildDiscoveredTrains(StationArrivalData stationArrivalData, OneDirectionBranchParseContext parseContext) {
            Map<String, List<DiscoveredTrain>> res = new HashMap<String, List<DiscoveredTrain>>();
            Map<String, List<String>> platformInfo = stationArrivalData.getPlatformInfo();

            for (Map.Entry<String,List<String>> entry : platformInfo.entrySet()) {
                List<String> platformTainInfos = entry.getValue();
                String platformName = entry.getKey();
                List<DiscoveredTrain> foundTrains = new ArrayList<DiscoveredTrain>();
                for (String platformTainInfo : platformTainInfos) {
                    LOG.info("info = " + platformTainInfo +"   on "+platformName);
                    if (platformTainInfo.length() > 0) {
                        DiscoveredTrain discoveredPoint = boardParsing.findPosition(
                                platformTainInfo,
                                stationArrivalData.stop.getStationName(),
                                stationArrivalData.concreteDirection,
                                parseContext.getBranch());
                        if (discoveredPoint != null) {
                            foundTrains.add(discoveredPoint);
                        }
                    } else {
                        LOG.warn("got no time info from stop " + parseContext.getLastBranchStop().getStation().getName() + " missing out for now");
                        //TODO cope with no info; estimate the position
                    }
                }
                res.put(platformName, foundTrains);
            }
            return res;
        }

        /**
         * For stations with muliple arrival borads, some arrival borards show trains that are
         * further away so this method finds the arrival board with the furthest away train and discards it.
         *
         * @return
         */
        private List<DiscoveredTrain> findPlatformWithSmallestRange(Map<String, List<DiscoveredTrain>> trainsAtPlatforms, AbstractDirection direction) {
            if (trainsAtPlatforms.isEmpty() || areAllEmpty(trainsAtPlatforms.values())) {
                return Collections.emptyList();
            } else if (trainsAtPlatforms.size() == 1) {
                return trainsAtPlatforms.values().iterator().next();
            } else {
                Map<String, DiscoveredTrain> furthestAwayForPlatforms = findFurthestAwayTrainsOnPlatforms(trainsAtPlatforms);
                BiMap<String, DiscoveredTrain> biMap = HashBiMap.create(furthestAwayForPlatforms);
                DiscoveredTrain closest = findClosestToStart(furthestAwayForPlatforms.values(), direction);
                return trainsAtPlatforms.get(biMap.inverse().get(closest));
            }
        }

        private <T extends Collection<?>> boolean areAllEmpty(Collection<T> collection){
            for (Collection<?> coll : collection) {
                if(coll.size() > 0)return false;
            }
            return true;
        }

        private DiscoveredTrain findClosestToStart(Collection<DiscoveredTrain> trains, AbstractDirection direction){
            Iterator<DiscoveredTrain> iterator = trains.iterator();
            if(!iterator.hasNext()){
                System.out.println("BranchIteratorImpl$ResultBuilder.findClosestToStart does not have next");
            }
            DiscoveredTrain platformContender = iterator.next();

            while (iterator.hasNext()) {
                DiscoveredTrain train = iterator.next();
                if(doesStationAppearBefore(train.getFurthestStation(), platformContender.getFurthestStation(), direction)){
                    platformContender = train;
                }
            }
            return platformContender;
        }

        /**
         * Also prevents duplicate values so that if the map is bimapped there are no duplicate keys
         *
         * @param trainsAtPlatforms
         * @return
         */
        private Map<String, DiscoveredTrain> findFurthestAwayTrainsOnPlatforms(Map<String, List<DiscoveredTrain>> trainsAtPlatforms) {
            Map<String, DiscoveredTrain> furthestAwayForPlatforms = new HashMap<String, DiscoveredTrain>();
            Set<DiscoveredTrain> foundTrains = Sets.newHashSet();
            for (String platform : trainsAtPlatforms.keySet()) {
                DiscoveredTrain furthestAway = getLastEntryInList(trainsAtPlatforms.get(platform));
                if(furthestAway != null && !foundTrains.contains(furthestAway)){
                    foundTrains.add(furthestAway);
                    furthestAwayForPlatforms.put(platform, furthestAway);
                }
            }
            return furthestAwayForPlatforms;
        }

        private <T> T getLastEntryInList(List<T> list){
            if(list.size() == 0){
                return null;
            } else{
                return list.get(list.size()-1);
            }
        }

        /**
         * Finds out if the station appears after or not
         * <p/>
         * For bakerloo line it actually says some trains are after a station traveling away from it!
         * looks like a defect but we need to ignore them.
         *
         * @return
         */
        private boolean doesStationAppearBefore(BranchStop discovered, BranchStop currentInMainIteration, AbstractDirection direction) {
            DirectionalBranchStopIterator iterator = DirectionalBranchStopIterator.FACTORY.all(branchStops, direction);
            return iterator.comesBefore(currentInMainIteration, discovered);
        }

        private String dump(StationArrivalData stationArrivalData) {
            StringBuilder builder = new StringBuilder("lastTrainToBeAdded is null, dumping data");
            builder.append("board data dump:\n" + stationArrivalData.dump() + "\n");
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