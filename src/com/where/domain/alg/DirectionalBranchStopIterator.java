package com.where.domain.alg;

import com.where.domain.BranchStop;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

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

    private final List<BranchStop> branchStops;
    private int nextIndex = 1;

    public DirectionalBranchStopIterator(List<BranchStop> branchStops, AbstractDirection abstractDirection) {
        this.branchStops = new ArrayList(branchStops);

        if (abstractDirection == AbstractDirection.ONE) { // iterate backwards
            Collections.reverse(this.branchStops);
        }
    }

    public boolean hasNext() {
        return nextIndex != branchStops.size()-1;
    }

    public BranchStop next() {
        BranchStop toReturn = branchStops.get(nextIndex);
        nextIndex++;
        return toReturn;
    }

    /** will return null if at end */
    public BranchStop peek() {
        if(!hasNext())return null;
        
        return branchStops.get(nextIndex);
    }

    public void remove() {
        throw new UnsupportedOperationException("can't remove");
    }

    /** assumes they are both in underlying array */
    public boolean comesAfter(BranchStop start, BranchStop query){
        int startIndex = branchStops.indexOf(start);

        for(int i=startIndex; i<branchStops.size();i++){
            if(branchStops.get(i).equals(query)){
                return true;
            }
        }
        return false;
    }

    /**
     * set the iterator to the end so the next time hasNext() is called it returns false
     */
    public void updateToEnd(){
       nextIndex = branchStops.size()-1;
    }

    /** allowed to go backwards? */
    public void updateTo(BranchStop next){
        for(int i=0; i<branchStops.size(); i++ ){
            //System.out.println("DirectionalBranchStopIterator.updateTo comparing "+branchStops.get(i).getStation().getName() +" and target "+next.getStation().getName());
            if(branchStops.get(i).getStation().getName().equals(next.getStation().getName())){
                LOG.debug("DirectionalBranchStopIterator.updateTo updating index from "+nextIndex+" to "+i);
                nextIndex = i;
                return;
            }
        }

        throw new IllegalStateException("Did not find branch stop '" + next.getStation().getName() + "' in list of branches");
            
    }
}
