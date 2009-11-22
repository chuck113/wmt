package com.where.domain.alg;

import com.where.domain.Point;

import java.util.List;
import java.util.LinkedHashMap;

/**
 * @author Charles Kubicek
 */
public interface BranchIterator {
    LinkedHashMap<AbstractDirection, List<Point>> run(String branchName);
}
