package com.where.core;

import com.where.domain.alg.DirectionalBranchStopIterator;
import com.where.domain.alg.AbstractDirection;
import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.domain.BranchStop;
import com.where.domain.Branch;

import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * @author Charles Kubicek
 */
public class DirectionalBranchStopIteratorTest extends TestCase {

    private DirectionalBranchStopIterator directionalBranchStopIterator;
    private WhereFixture fixture;
    private List<BranchStop> stops;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        this.fixture = new WhereFixture();
        Branch branch1 = fixture.getSerializedFileDaoFactory().getBranchDao().getBranch("victoria");
        stops = fixture.getSerializedFileDaoFactory().getBranchDao().getBranchStops(branch1);
    }

    public void testDirectionalBranchStopIteratorForAlgorithm() {
        testDirectionalBranchStopIteratorForAlgorithm(AbstractDirection.TWO, stops.iterator());
        List<BranchStop> reversed = new ArrayList<BranchStop>(stops);
        Collections.reverse(reversed);
        testDirectionalBranchStopIteratorForAlgorithm(AbstractDirection.ONE, reversed.iterator());
    }

    private void testDirectionalBranchStopIteratorForAlgorithm(AbstractDirection dir, Iterator<BranchStop> iter) {
        DirectionalBranchStopIterator dirIter = makeAlg(dir);
        iter.next();

        while (dirIter.hasNext()) {
            assert (dirIter.hasNext());
            assertEquals(iter.next(), dirIter.next());
        }

        assert (iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    private void testDirectionalBranchStopIteratorForAll(AbstractDirection dir, Iterator<BranchStop> iter) {
        DirectionalBranchStopIterator dirIter = makeAll(dir);

        while (dirIter.hasNext() && dirIter.hasNext()) {
            assertEquals(iter.next().getStation().getName(), dirIter.next().getStation().getName());
        }

        assertFalse(iter.hasNext());
        assertFalse(dirIter.hasNext());
    }

    public void testDirectionalBranchStopIteratorForAll() {
        testDirectionalBranchStopIteratorForAll(AbstractDirection.TWO, stops.iterator());
        List<BranchStop> reversed = new ArrayList<BranchStop>(stops);
        Collections.reverse(reversed);
        testDirectionalBranchStopIteratorForAll(AbstractDirection.ONE, reversed.iterator());
    }

    public void testComesBefore() {
        comesBefore(makeAlg(AbstractDirection.ONE), AbstractDirection.ONE);
        comesBefore(makeAlg(AbstractDirection.TWO), AbstractDirection.TWO);
        comesBefore(makeAll(AbstractDirection.ONE), AbstractDirection.ONE);
        comesBefore(makeAll(AbstractDirection.TWO), AbstractDirection.TWO);
    }

    public void testComesBeforeForSameStation() {
        comesBeforeForSameStation(makeAlg(AbstractDirection.ONE), AbstractDirection.ONE);
        comesBeforeForSameStation(makeAlg(AbstractDirection.TWO), AbstractDirection.TWO);
        comesBeforeForSameStation(makeAll(AbstractDirection.ONE), AbstractDirection.ONE);
        comesBeforeForSameStation(makeAll(AbstractDirection.TWO), AbstractDirection.TWO);
    }

    public void testComesAfterForSameStation() {
        comesAfterForSameStation(makeAlg(AbstractDirection.ONE), AbstractDirection.ONE);
        comesAfterForSameStation(makeAlg(AbstractDirection.TWO), AbstractDirection.TWO);
        comesAfterForSameStation(makeAll(AbstractDirection.ONE), AbstractDirection.ONE);
        comesAfterForSameStation(makeAll(AbstractDirection.TWO), AbstractDirection.TWO);
    }

    public void testComesAfter() {
        comesAfter(makeAlg(AbstractDirection.ONE), AbstractDirection.ONE);
        comesAfter(makeAlg(AbstractDirection.TWO), AbstractDirection.TWO);
        comesAfter(makeAll(AbstractDirection.ONE), AbstractDirection.ONE);
        comesAfter(makeAll(AbstractDirection.TWO), AbstractDirection.TWO);
    }

    private void comesAfterForSameStation(DirectionalBranchStopIterator dirIter, AbstractDirection dir) {
        BranchStop bs2 = stops.get(2);

        if (dir == AbstractDirection.TWO)
            assertFalse(dirIter.comesAfter(bs2, bs2));
        else {
            assertFalse(dirIter.comesAfter(bs2, bs2));
        }
    }

    private void comesBeforeForSameStation(DirectionalBranchStopIterator dirIter, AbstractDirection dir) {
        BranchStop bs2 = stops.get(2);

        if (dir == AbstractDirection.TWO)
            assertFalse(dirIter.comesBefore(bs2, bs2));
        else {
            assertFalse(dirIter.comesBefore(bs2, bs2));
        }
    }

    private void comesAfter(DirectionalBranchStopIterator dirIter, AbstractDirection dir) {
        BranchStop bs2 = stops.get(1);
        BranchStop bs8 = stops.get(7);

        if (dir == AbstractDirection.TWO)
            assertTrue(dirIter.comesAfter(bs2, bs8));
        else {
            assertFalse(dirIter.comesAfter(bs2, bs8));
        }
    }

    private void comesBefore(DirectionalBranchStopIterator dirIter, AbstractDirection dir) {
        BranchStop bs2 = stops.get(1);
        BranchStop bs8 = stops.get(7);

        if (dir == AbstractDirection.TWO)
            assertFalse(dirIter.comesBefore(bs2, bs8));
        else {
            assertTrue(dirIter.comesBefore(bs2, bs8));
        }
    }

    public void testGoToEnd() {
        DirectionalBranchStopIterator dirIter = makeAlg(AbstractDirection.ONE);
        dirIter.updateToEnd();
        ;
        assertFalse(dirIter.hasNext());
    }

    public void testUpdateTo() {
        Iterator<BranchStop> stdIter = stops.iterator();
        DirectionalBranchStopIterator dirIter = makeAlg(AbstractDirection.TWO);
        stdIter.next();  // skip 1st as this is an alg iterator - misses first stop out

        assertEquals(stdIter.next(), dirIter.next()); //check the iterator is moving in the right direction 

        stdIter.next();
        stdIter.next();

        BranchStop toUpdateTo = stdIter.next();

        dirIter.updateTo(toUpdateTo);
        assertEquals(stdIter.next(), dirIter.next());
    }

    public void testSetNext() {
        Iterator<BranchStop> stdIter = stops.iterator();
        DirectionalBranchStopIterator dirIter = makeAlg(AbstractDirection.TWO);
        stdIter.next();  // skip 1st as this is an alg iterator - misses first stop out

        assertEquals(stdIter.next(), dirIter.next()); //check the iterator is moving in the right direction

        stdIter.next();
        stdIter.next();

        BranchStop toUpdateTo = stdIter.next();

        dirIter.setNext(toUpdateTo);
        assertEquals(toUpdateTo, dirIter.next());
    }

    public void testPeek() {
        DirectionalBranchStopIterator dirIter = makeAlg(AbstractDirection.TWO);
        dirIter.next();

        BranchStop stop = dirIter.peek();
        assertEquals(stop, dirIter.next());
        assertNotSame(stop, dirIter.next());
    }

    private DirectionalBranchStopIterator makeAlg(AbstractDirection dir) {
        return DirectionalBranchStopIterator.FACTORY.forAlgorithm(stops, dir);
    }

    private DirectionalBranchStopIterator makeAll(AbstractDirection dir) {
        return DirectionalBranchStopIterator.FACTORY.all(stops, dir);
    }

}
