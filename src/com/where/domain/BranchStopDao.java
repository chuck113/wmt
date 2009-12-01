package com.where.domain;

import com.where.domain.Memcachable;

/**
 * @author Charles Kubicek
 */
public interface BranchStopDao extends Memcachable {
    FindBranchStopResult getBranchStop(String name, Branch branch);
}
