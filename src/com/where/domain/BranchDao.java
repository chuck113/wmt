package com.where.domain;

import com.where.collect.OrderedMap;

import java.util.List;

/**
 * @author Charles Kubicek
 */
public interface BranchDao {

    public Branch getBranch(String name);

    public List<BranchStop> getBranchStops(Branch branch);

    public OrderedMap<BranchStop> getIndexedBranchStops(Branch branch);
}
