package com.where.domain.alg;

import com.where.domain.Branch;
import com.where.domain.BranchStop;

import java.util.List;

/**
 * @author Charles Kubicek
 *
 * Keeps objects and methods relevant for one parse
 */
public class BranchParseContext {
    private final Branch branch;
    private final AbstractDirection direction;
    private final List<BranchStop> branchStops;

    public BranchParseContext(Branch branch, AbstractDirection direction, List<BranchStop> branchStops) {
        this.branch = branch;
        this.direction = direction;
        this.branchStops = branchStops;
    }
}
