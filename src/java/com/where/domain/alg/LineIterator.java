package com.where.domain.alg;

import com.where.domain.Point;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 23-Nov-2009
 * Time: 20:48:27
 * To change this template use File | Settings | File Templates.
 */
public interface LineIterator {
    SetMultimap<AbstractDirection,Point> run(String lineName);
}
