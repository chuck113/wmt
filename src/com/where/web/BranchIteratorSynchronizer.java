package com.where.web;

/**
 * Deals with getting a new branch in a multi-threaded environment, ensures
 * that only one thread does the parse while the other wait. May or may
 * not cache results - but probaly will.
 *
 * @author Charles Kubicek
 */
public interface BranchIteratorSynchronizer {

    String getBranch(String branch);
}
