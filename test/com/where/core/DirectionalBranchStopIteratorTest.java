package com.where.core;

import com.where.domain.alg.DirectionalBranchStopIterator;
import com.where.domain.alg.AbstractDirection;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.domain.BranchStop;
import com.where.domain.Branch;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author Charles Kubicek
 */
public class DirectionalBranchStopIteratorTest extends TestCase {

    private DirectionalBranchStopIterator directionalBranchStopIterator;
    private WhereFixture fixture;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        this.fixture = new WhereFixture();
    }

    public void testDirectionalBranchStopIterator1(){
        List<BranchStop> stops = getStops("victoria");
        directionalBranchStopIterator = new DirectionalBranchStopIterator(stops, AbstractDirection.ONE);

        assertTrue(directionalBranchStopIterator.hasNext());
        while(directionalBranchStopIterator.hasNext()){
            System.out.println("next is "+directionalBranchStopIterator.next().getStation().getName());
        }

    }

    private List<BranchStop> getStops(String branch){
        Branch branch1 = fixture.getSerializedFileDaoFactory().getBranchDao().getBranch(branch);
        return fixture.getSerializedFileDaoFactory().getBranchDao().getBranchStops(branch1);
    }

     public void testDirectionalBranchStopIterator2(){
        List<BranchStop> stops = getStops("victoria");
         directionalBranchStopIterator = new DirectionalBranchStopIterator(stops, AbstractDirection.TWO);

        assertTrue(directionalBranchStopIterator.hasNext());
        while(directionalBranchStopIterator.hasNext()){
            System.out.println("next is "+directionalBranchStopIterator.next().getStation().getName());
        }

        BranchStop firstStop = stops.get(0);
        directionalBranchStopIterator = new DirectionalBranchStopIterator(stops, AbstractDirection.ONE);
         directionalBranchStopIterator.updateTo(firstStop);
         assertFalse(directionalBranchStopIterator.hasNext());

    }
}
