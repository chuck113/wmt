package com.where.domain.alg;

import com.where.domain.BranchStop;
import com.where.collect.OrderedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * @author Charles Kubicek
 *
 * An attempt to abstract away the difficulties of dealing with directions with an iterator.
 * Given a direction this will always return the next stop for that direction, and it is allowed
 * to be udpated.
 */
public class DirectionalBranchStopIterator implements Iterator<BranchStop>{
    private static final Logger LOG = Logger.getLogger(DirectionalBranchStopIterator.class);

    private final int endIndex;

   // private final List<BranchStop> oldBranchStops;
    private final OrderedMap<BranchStop> newBranchStops;
    private int nextIndex;

    public static final Factory FACTORY = new Factory();

    public static class Factory{

        /**
         * An iterator that start one element from the start and ends one from the end
         */
        public DirectionalBranchStopIterator forAlgorithm(List<BranchStop> branchStops, AbstractDirection direction){
            return new DirectionalBranchStopIterator(branchStops, direction, Mode.ALGORITHM);
        }

        /**
         * An iterator that iterats over all the entries
         */
        public DirectionalBranchStopIterator all(List<BranchStop> branchStops, AbstractDirection direction){
            return new DirectionalBranchStopIterator(branchStops, direction, Mode.FULL);
        }
    }

    private static enum Mode{
        FULL(0), ALGORITHM(1);
        private final int startOffset;

        private Mode(int startEndOffset) {
            this.startOffset = startEndOffset;
        }

        public int getEndIndex(List<BranchStop> branchStops){
            return branchStops.size() -1 -startOffset;
        }

        public int getStartInext(){
            return startOffset;
        }
    }

    private DirectionalBranchStopIterator(List<BranchStop> branchStops, AbstractDirection abstractDirection, Mode mode) {
        this.endIndex = mode.getEndIndex(branchStops);
        this.nextIndex = mode.getStartInext();
        newBranchStops = new OrderedMap<BranchStop>(abstractDirection.makeIterable(branchStops));
    }


    public boolean hasNext() {
        return nextIndex <= endIndex;
    }

    public BranchStop next() {
        BranchStop toReturn = newBranchStops.get(nextIndex);
        nextIndex++;
        return toReturn;
        //TODO test newBranchStops.get(++nextIndex);
    }

    /** will return null if at end */
    public BranchStop peek() {
        if(!hasNext())return null;
        
        return newBranchStops.get(nextIndex);
    }

    public void remove() {
        throw new UnsupportedOperationException("can't remove");
    }

    /** assumes they are both in underlying array */
    public boolean comesAfter(BranchStop point, BranchStop appearsAfterPoint){
        return newBranchStops.indexOf(point) < newBranchStops.indexOf(appearsAfterPoint);
    }

    public boolean comesBefore(BranchStop point, BranchStop doesThisAppearBeforePoint){
        return newBranchStops.indexOf(point) > newBranchStops.indexOf(doesThisAppearBeforePoint);
    }

    /**
     * set the iterator to the end so the next time hasNext() is called it returns false
     */
    public void updateToEnd(){
        LOG.debug("updating iterator to end (index "+endIndex+1+")");
        nextIndex = endIndex+1;
    }

    /**
     * Updates the iterator so the next returned is the *stop after* the given branchStop, updates the
     * iterator as if the last next() call returned the given stop
     *
     * allowed to go backwards?
     */
    public void updateTo(BranchStop next){
        LOG.debug("updating iterator station '"+next.getStationName()+"' (index "+(newBranchStops.indexOf(next)+1)+")");
        nextIndex = newBranchStops.indexOf(next)+1;
    }

    /**
     * Updates the iterator so the next value is midway between the current iterator and the
     * end, used to miss out a few stops when we supect the branch is closed
     */
    public void updateMidway(){
        LOG.debug("updating iterator to midway (index "+(nextIndex + ((endIndex - nextIndex) / 2))+")");
        nextIndex = nextIndex + ((endIndex - nextIndex) / 2);
    }

    /**
     * Updates the iterator so the next returned is the the given branchStop
     */
    public void setNext(BranchStop next){
        LOG.debug("setting next to '"+next.getStationName()+"' (index "+(newBranchStops.indexOf(next))+")");
        nextIndex = newBranchStops.indexOf(next);
    }
}
