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
        System.out.println("DirectionalBranchStopIterator.next nex index is: "+nextIndex);
        BranchStop toReturn = newBranchStops.get(nextIndex);
        nextIndex++;
        return toReturn;
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
    public boolean comesAfter(BranchStop start, BranchStop query){        
        return newBranchStops.indexOf(start) < newBranchStops.indexOf(query);

//        int startIndex = oldBranchStops.indexOf(start);
//
//        for(int i=startIndex; i<oldBranchStops.size();i++){
//            if(oldBranchStops.get(i).equals(query)){
//                return true;
//            }
//        }
//        return false;
    }

    /**
     * set the iterator to the end so the next time hasNext() is called it returns false
     */
    public void updateToEnd(){
        nextIndex = endIndex+1;
    }

    /**
     * Updates the iterator so the next returned is the *stop after* the given branchStop, updates the
     * iterator as if the last next() call returned the given stop
     *
     * allowed to go backwards?
     */
    public void updateTo(BranchStop next){
        nextIndex = newBranchStops.indexOf(next)+1;
        System.out.println("DirectionalBranchStopIterator.updateTo set index to "+nextIndex+"/"+endIndex+" for stop: "+next);
    }

    /**
     * Updates the iterator so the next returned is the the given branchStop
     */
    public void setNext(BranchStop next){
        nextIndex = newBranchStops.indexOf(next);
        System.out.println("DirectionalBranchStopIterator.setNext set index to "+nextIndex+"/"+endIndex+" for stop: "+next + ", has next: "+hasNext());
    }
}
