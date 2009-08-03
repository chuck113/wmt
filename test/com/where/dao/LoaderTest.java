package com.where.dao;
/**
 * */

import junit.framework.*;
import com.where.dao.hsqldb.HibernateLoader;
import com.where.dao.hsqldb.*;
import com.where.hibernate.Branch;
import com.where.hibernate.BranchStop;

import java.util.List;

public class LoaderTest extends TestCase {
  HibernateLoader loader;

  protected void setUp() throws Exception {
    super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
  }

  public void testLoad() throws Exception {
      DataMapper dataMapper = new DataMapperImpl(new SerializedFileLoader(SerializedFileLoader.DATA_FOLDER_NAME));
      Branch branch = dataMapper.getBranchNamesToBranches().get("victoria");
      List<BranchStop> stops1 = dataMapper.getBranchStops(branch);
      List<BranchStop> stops2 = dataMapper.getBranchStops(branch);

      System.out.println("LoaderTest.testLoad stop1 "+stops1.get(0).getStation().getName());
      System.out.println("LoaderTest.testLoad stops2 "+stops2.get(0).getStation().getName());
  }
}