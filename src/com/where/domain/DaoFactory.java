package com.where.domain;

/**
 * @author Charles Kubicek
 */
public interface DaoFactory {

    BranchDao getBranchDao();

    BranchStopDao getBranchStopDao();
}
