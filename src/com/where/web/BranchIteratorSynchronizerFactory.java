package com.where.web;

import com.where.domain.alg.BranchIterator;

/**
 * @author Charles Kubicek
 */
public interface BranchIteratorSynchronizerFactory {
    BranchIteratorSynchronizer build(BranchIterator branchIterator);
}
