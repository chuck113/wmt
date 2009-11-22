package ck.play.guice;

import com.where.domain.alg.BranchIterator;
import com.where.tfl.grabber.TFLSiteScraper;
import com.where.tfl.grabber.ArrivalBoardScraper;
import com.google.inject.Inject;

/**
 * @author Charles Kubicek
 */
public class MyService {
    private final ArrivalBoardScraper scaper;

    @Inject
    public MyService(ArrivalBoardScraper scaper) {
        this.scaper = scaper;
    }
}

