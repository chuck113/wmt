package com.where.core;
/**
 * */

import junit.framework.*;
import com.where.core.Algorithm;
import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Branch;
import com.where.dao.Loader;

/**
 * bank northern
 * 8 high barnet northern
 * 7 charing cross northern
 * 10 test cross northern
 * 11 test northern northern
 * 12 victoria victoria
 * 13 jubilee jubilee
 * 14 bakerloo bakerloo
 * 15 metropolitan metropolitan
 */
public class AlgorithmTest extends TestCase {
  Algorithm algorithm;

  protected void setUp() throws Exception {
    super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
  }

  public void testRun() throws Exception {
   Branch branch = Loader.instance().getBranchNamesToBranches().get("victoria");
      System.out.println("AlgorithmTest.testRun branch: "+branch);
   algorithm = new Algorithm("victoria");

   algorithm.iterateForDirection(branch, AbstractDirection.ONE);
  }

  private void testPosition(String position, String stationAt, String furthestPos){
    algorithm = new Algorithm("victoria");
    Position pos = algorithm.makePosition(position, stationAt);
    BranchStop stop = pos.findFurthest();
    assertEquals(furthestPos, stop.getStation().getName());
  }

  //Unknown
  //At Platform
  //Near High Barnet
  //Leaving Euston
  public void testMakePositionBetween() throws Exception {
     testPosition("Between High Barnet and Totteridge & Whetstone", null, "High Barnet");
  }

  public void testMakePositionAt() throws Exception {
     testPosition("At East Finchley Platform 4", null, "East Finchley");
  }

  public void testMakePositionBy() throws Exception {
     testPosition("By East Finchley", null, "East Finchley");
  }

  public void testMakePositionLeft() throws Exception {
     testPosition("Left East Finchley", null, "East Finchley");
  }

  public void testMakePositionLeaving() throws Exception {
     testPosition("Leaving East Finchley", null, "East Finchley");
  }

  public void testMakePositionApproaching() throws Exception {
     testPosition("Approaching East Finchley", null, "East Finchley");
  }

  //Leaving Waterloo towards Kennington
  public void testMakePositionLeavingTowards() throws Exception {
     testPosition("Leaving Waterloo towards Kennington", null, "Kennington");
  }
}