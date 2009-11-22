package com.where.web;

import com.where.domain.Point;
import com.where.domain.alg.AbstractDirection;

import java.util.Set;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashSet;

/**
 * @author Charles Kubicek
 */
public class ResultTransformer {

    public static String toJson(LinkedHashMap<AbstractDirection, List<Point>> points){
        return makeJsonPoints(convertPoints(points)).toString();
    }

    private static Set<Point> convertPoints(LinkedHashMap<AbstractDirection, List<Point>> points) {
        Set<Point> result = new HashSet<Point>();

        for (List<Point> ps : points.values()) {
            result.addAll(ps);
        }
        return result;
    }

    private static StringBuffer makeJsonPoints(Set<Point> points) {
        StringBuffer buf = new StringBuffer("{\"points\": { \"pointsArray\" : [\n");

        for (Point point : points) {
            buf.append("  { \"lat\" : " + point.getLat() + ", \"lng\" : " + point.getLng() + ", \"direction\" : \"" + point.getDirection().getName() + "\", \"description\" : \"" + point.getDescription() + "\"},\n");
        }
        buf.append("]}}");
        return buf;
    }
}
