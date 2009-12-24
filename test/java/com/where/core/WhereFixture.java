package com.where.core;

import com.where.dao.hsqldb.DataMapperImpl;
import com.where.dao.hsqldb.SerializedFileLoader;
import com.where.dao.hsqldb.DataMapper;
import com.where.domain.DaoFactory;
import com.where.domain.DataMapperDaoFactoryImpl;
import com.where.domain.Branch;

/**
 * @author Charles Kubicek
 */
public class WhereFixture {

    public static final String TEST_RESOURCES_FOLDER = "C:\\data\\projects\\wheresmytube\\wheresmytube\\test\\resources\\";
    public static final String TEST_HTMLS_FOLDER = TEST_RESOURCES_FOLDER+"test-htmls";
    public static final String HTMLS_LONG_FOLDER = TEST_RESOURCES_FOLDER+"htmls-long";
    public static final String HTMLS_FOLDER = TEST_RESOURCES_FOLDER+"htmls";

    private final DataMapper mapper;
    private final DaoFactory factory;

    public WhereFixture(){
        this.mapper = new DataMapperImpl(SerializedFileLoader.Factory.fromClassPath());
        this.factory = new DataMapperDaoFactoryImpl(mapper);
    }

    public DataMapper getSerializedFileDataMapper(){
        return mapper;
    }

    public DaoFactory getSerializedFileDaoFactory(){
        return factory;
    }

    public Branch victoriaBranch(){
        return factory.getLineDao().getLinesToBranches().get("victoria").iterator().next();
    }

    public Branch highBarnetBranch(){
        return factory.getBranchDao().getBranch("high barnet");
    }

    public Branch charingCrossBranch(){
        return factory.getBranchDao().getBranch("charing cross");
    }

//    INSERT INTO BRANCHES VALUES(7,'charing cross','northern')
//INSERT INTO BRANCHES VALUES(8,'high barnet','northern')
//INSERT INTO BRANCHES VALUES(9,'bank','northern')
//INSERT INTO BRANCHES VALUES(12,'victoria','victoria')
//INSERT INTO BRANCHES VALUES(13,'jubilee','jubilee')
//INSERT INTO BRANCHES VALUES(14,'bakerloo','bakerloo')
//INSERT INTO BRANCHES VALUES(15,'metropolitan','metropolitan')
//INSERT INTO BRANCHES VALUES(16,'hammersmith','hammersmith')
//INSERT INTO BRANCHES VALUES(17,'central','central')
//INSERT INTO BRANCHES VALUES(18,'fairlop loop','central')
//INSERT INTO BRANCHES VALUES(19,'ealing broadway','central')
//INSERT INTO BRANCHES VALUES(20,'picadilly','picadilly')
//INSERT INTO BRANCHES VALUES(21,'heathrow','picadilly')
}
