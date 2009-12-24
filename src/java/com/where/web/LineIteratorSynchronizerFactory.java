package com.where.web;

import com.where.domain.alg.BranchIterator;
import com.where.domain.alg.LineIterator;

/**
 * @author Charles Kubicek
 */
public interface LineIteratorSynchronizerFactory {
    LineIteratorSynchronizer build(LineIterator branchIterator);
}
