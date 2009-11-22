package com.where.web;

import com.where.domain.alg.BranchIterator;

/**
 * @author Charles Kubicek
 */
public class DefaultBranchIteratorSynchronizerFactoryImpl implements BranchIteratorSynchronizerFactory {
    public BranchIteratorSynchronizer build(BranchIterator branchIterator){
        return new BranchIteratorSynchronizerImpl(branchIterator);
    }
}
