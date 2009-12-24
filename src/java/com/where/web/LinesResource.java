package com.where.web;

import com.where.domain.alg.LineIteratorImpl;
import com.where.domain.DaoFactory;
import com.where.tfl.grabber.ArrivalBoardScraper;

public class LinesResource extends AbstractLinesResource{

    // static so can re-use between requests if jvm is cached
    private/* final*/ static LineIteratorSynchronizer lineSyncer;
    private final DaoFactory daoFactory;

    public LinesResource() {
        daoFactory = new ClasspathFileDataSource().getDaoFactory();
    }

    LineIteratorSynchronizer getLineIteratorSynchronizer(ArrivalBoardScraper scraper, DaoFactory daoFactory) {
        if (lineSyncer == null) {
            try {
                lineSyncer = PropsReader.buildLineIteratorSynchronizerFactoryInstance().build(
                        new LineIteratorImpl(daoFactory, scraper));
            } catch (Exception e) {
                e.printStackTrace();
                lineSyncer = new DefaultLineIteratorSynchronizerFactoryImpl().build(new LineIteratorImpl(getDaoFactory(), scraper));
            }
        }
        return lineSyncer;
    }

    DaoFactory getDaoFactory() {
        return daoFactory;
    }
}
