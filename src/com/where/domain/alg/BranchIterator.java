package com.where.domain.alg;

import com.where.domain.Point;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.List;
import java.util.LinkedHashMap;

/**
 * @author Charles Kubicek
 */
public interface BranchIterator {
    SetMultimap<AbstractDirection,Point> run(String branchName);
}
