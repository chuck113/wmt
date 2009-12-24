package com.where.domain.alg;

import com.where.domain.*;
import com.where.tfl.grabber.ArrivalBoardScraper;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 */
public class LineIteratorImpl implements LineIterator {

    private Logger LOG = Logger.getLogger(LineIteratorImpl.class);

    private final LineDao lineDao;
    private final BranchIterator branchIterator;

    public LineIteratorImpl(DaoFactory daoFactory, ArrivalBoardScraper scraper) {
        this.lineDao = daoFactory.getLineDao();
        branchIterator = new BranchIteratorImpl(daoFactory, scraper);
    }

    public SetMultimap<AbstractDirection,Point> run(String lineName) {
        LinkedHashMultimap<AbstractDirection,Point> res = LinkedHashMultimap.create();
        Set<Branch> branches = lineDao.getLinesToBranches().get(lineName);
        for (Branch branch : branches) {
            System.out.println("LineIteratorImpl.run parsing branch: "+branch.getName());
            res.putAll(branchIterator.run(branch.getName()));
        }
        return res;
    }
}
