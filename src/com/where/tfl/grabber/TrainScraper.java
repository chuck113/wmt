package com.where.tfl.grabber;

import com.where.dao.hibernate.BranchStop;
import com.where.dao.hibernate.Branch;

/**
 * @author Charles Kubicek
 */
public interface TrainScraper {
    BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException;
}
