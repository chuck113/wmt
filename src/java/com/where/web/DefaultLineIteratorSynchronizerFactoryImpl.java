package com.where.web;

import com.where.domain.alg.BranchIterator;
import com.where.domain.alg.LineIterator;

/**
 * @author Charles Kubicek
 */
public class DefaultLineIteratorSynchronizerFactoryImpl implements LineIteratorSynchronizerFactory {

    public LineIteratorSynchronizer build(LineIterator branchIterator) {
        return new LineIteratorSynchronizerImpl(branchIterator);
    }
}
