package com.where.dao.hsqldb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Charles Kubicek
 */
public class NumberGenerator {

    private static AtomicInteger next = new AtomicInteger(0);

    public static int next(){
        return next.incrementAndGet();
    }
}
