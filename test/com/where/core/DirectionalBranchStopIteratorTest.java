package com.where.core;

import com.where.domain.alg.DirectionalBranchStopIterator;
import com.where.domain.alg.AbstractDirection;
import com.where.dao.DataMapperImpl;
import com.where.dao.SerializedFileLoader;
import com.where.dao.hibernate.BranchStop;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author Charles Kubicek
 */
public class DirectionalBranchStopIteratorTest extends TestCase {

    private DirectionalBranchStopIterator directionalBranchStopIterator;

    public void testDirectionalBranchStopIterator1(){
        DataMapperImpl mapper = new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME));
        List<BranchStop> stops = mapper.getBranchStops(mapper.getBranchNamesToBranches().get("victoria"));
        directionalBranchStopIterator = new DirectionalBranchStopIterator(stops, AbstractDirection.ONE);

        assertTrue(directionalBranchStopIterator.hasNext());
        while(directionalBranchStopIterator.hasNext()){
            System.out.println("next is "+directionalBranchStopIterator.next().getStation().getName());
        }

    }

     public void testDirectionalBranchStopIterator2(){
        DataMapperImpl mapper = new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME));
        List<BranchStop> stops = mapper.getBranchStops(mapper.getBranchNamesToBranches().get("victoria"));
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
