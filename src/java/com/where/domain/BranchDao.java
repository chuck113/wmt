package com.where.domain;

import com.where.collect.OrderedMap;
import com.where.domain.Memcachable;

import java.util.List;

/**
 * @author Charles Kubicek
 */
public interface BranchDao extends Memcachable {

    Branch getBranch(String name);

    List<BranchStop> getBranchStops(Branch branch);

    OrderedMap<BranchStop> getIndexedBranchStops(Branch branch);
}
