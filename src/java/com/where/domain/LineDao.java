package com.where.domain;

import com.google.common.collect.LinkedHashMultimap;
import com.where.domain.Memcachable;

/**
 * @deprecated never used
 */
public interface LineDao extends Memcachable{
    Line getLine(String line);
    LinkedHashMultimap<String, Branch> getLinesToBranches();
    boolean isStationOnLineAndNotOnBranch(Branch branch, String stationName);
}
