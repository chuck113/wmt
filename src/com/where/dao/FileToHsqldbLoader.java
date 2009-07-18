package com.where.dao;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.where.domain.Branch;
import com.where.domain.BranchStop;

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 17-Jun-2009
 * Time: 21:33:32
 * To change this template use File | Settings | File Templates.
 */
public class FileToHsqldbLoader implements HibernateLoader {
    public static final String JAVADB_LOCATION = System.getProperty("java.io.tmpdir") + File.separator + "tubedata";

    private final Map<Branch, List<BranchStop>> branchesToBranchStops;
    private final Map<BranchStop, Branch> branchStopsToBranches;
    private final Map<String, Branch> branchNamesToBranches;
    private final Map<String, BranchStop> stationNamesToBrancheStops;

    private final Map<Integer, Branch> branchTable = new HashMap<Integer, Branch>();

    public FileToHsqldbLoader() {
        this.branchesToBranchStops = new HashMap<Branch, List<BranchStop>>();
        this.branchStopsToBranches = new HashMap<BranchStop, Branch>();
        this.branchNamesToBranches = new HashMap<String, Branch>();
        this.stationNamesToBrancheStops = new HashMap<String, BranchStop>();
    }


    public void load() {
            try {
                //File dataFile = FileUtils.toFile(this.getClass().getClassLoader().getResource("tubedata-formatted.sql"));
                List<String> lines = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream("tubedata-formatted-hsqldb.sql"));
                Class.forName("org.hsqldb.jdbcDriver");
                Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:" + "test", "sa", "");

                //
                for (String line : lines) {
                    if (line.startsWith("CREATE") || line.startsWith("INSERT")) {
                        String sanitizedLine = line.replace("`", "");
                        System.out.println("FileToHsqldbLoader.load executing line: " + sanitizedLine);
                        conn.createStatement().execute(sanitizedLine);
                    }
                }

                //CREATE TABLE branches (`id` int IDENTITY, `name` varchar(50) NOT NULL , line varchar(50) NOT NULL , PRIMARY KEY (id));

                ResultSet resultSet = conn.createStatement().executeQuery("select * from branches");

                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String line = resultSet.getString("line");
                    Branch branch = new Branch(id, name, line);
                    branchTable.put(id, branch);
                    this.branchNamesToBranches.put(name, branch);
                }

                resultSet = conn.createStatement().executeQuery("select * from branch_stops");

                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String station_id = resultSet.getString("station_id");
                    String order_no = resultSet.getString("order_no");
                    String branch_id = resultSet.getString("branch_id");
                   // BranchStop bs = new BranchStop(id, order_no, )
                    //this.stationNamesToBrancheStops.put(name, new BranchStop(id, name, line));
                }

            resultSet = conn.createStatement().executeQuery("select * from branches");

            } catch (/*IO*/Exception e) {
                e.printStackTrace();
            }

    }

    public Map<BranchStop, Branch> getBranchStopsToBranches() {
        return branchStopsToBranches;
    }

    public BranchStop getBranchStopFromStationName(String stationName) {
        return stationNamesToBrancheStops.get(stationName);
    }

    public Map<String, Branch> getBranchNamesToBranches() {
        return branchNamesToBranches;
    }

    public List<BranchStop> getBranchStops(Branch branch) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String[] args) {
        new FileToHsqldbLoader().load();
    }
}
