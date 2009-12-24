package com.where.domain;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.HashMultimap;

import java.util.List;
import java.util.Map;

/**
 */
public class DataMapperLineDao implements LineDao {

    public DataMapperLineDao(LinkedHashMultimap<String, Branch> linesToBranches, Map<Branch, List<BranchStop>> branchToBranchStops) {
        this.linesToBranches = linesToBranches;
        this.linesToStationNames = buildInverseLineToBranchStops(linesToBranches, branchToBranchStops);
        this. branchesToStationNames = buildInverseBranchToBranchStops(branchToBranchStops);
    }

    private final LinkedHashMultimap<String, Branch> linesToBranches;
    private final HashMultimap<String, String> linesToStationNames;
    private final HashMultimap<Branch, String> branchesToStationNames;
    
    private HashMultimap<String, String> buildInverseLineToBranchStops(
            LinkedHashMultimap<String, Branch> linesToBranches,
            Map<Branch, List<BranchStop>> branchToBranchStops){

        HashMultimap<String, String> res = HashMultimap.create();


        //taken out because this isn't used, will remove soon
//        for(String line : linesToBranches.keySet()){
//            for(Branch branch : linesToBranches.get(line)){
//                for(BranchStop stop : branchToBranchStops.get(branch)){
//                    res.put(line, stop.getStationName());
//                }
//            }
//        }

        return res;
    }

    private HashMultimap<Branch, String> buildInverseBranchToBranchStops(
            Map<Branch, List<BranchStop>> branchToBranchStops){

        HashMultimap<Branch, String> res = HashMultimap.create();

        //taken out because this isn't used, will remove soon
//        for(Map.Entry<Branch,List<BranchStop>> entry : branchToBranchStops.entrySet()){
//            for(BranchStop stop : entry.getValue()){
//                res.put(entry.getKey(), stop.getStationName());
//            }
//        }

        return res;
    }

    public LinkedHashMultimap<String, Branch> getLinesToBranches() {
        return linesToBranches;
    }

    public Line getLine(String line){
        System.err.println("Using DataMapperLineDao whic doesn't work");
                
        return new Line(line);
    }

    public boolean isStationOnLineAndNotOnBranch(Branch branch, String station){
        System.err.println("Using DataMapperLineDao whic doesn't work");

        if(linesToStationNames.get(branch.getLine()).contains(station)){
            if(!branchesToStationNames.get(branch).contains(station)){
                System.out.println("DataMapperLineDao.isStationOnLineAndNotOnBranch TRUE line: "+branch.getLine()+", branch: "+branch.getName()+" station: "+station);
                return true;
            }
        }

        return false;
    }
}
