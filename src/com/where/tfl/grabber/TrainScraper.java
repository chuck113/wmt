package com.where.tfl.grabber;

import com.where.domain.BranchStop;
import com.where.domain.Branch;

/**
 * @author Charles Kubicek
 */
public interface TrainScraper {
    BoardParserResult get(BranchStop branchStop, Branch branch) throws ParseException;
}
