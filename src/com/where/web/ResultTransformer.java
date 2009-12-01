package com.where.web;

import com.where.domain.Point;
import com.where.domain.Direction;
import com.where.domain.alg.AbstractDirection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.*;
import java.text.DecimalFormat;

/**
 * @author Charles Kubicek
 *         <p/>
 *         {"points": { "pointsArray" : [
 *         { "lat" : 51.485412516629566, "lng" : -0.12172630804428243, "direction" : "Southbound", "description" : "At Vauxhall Platform 2"}
 *         ]}}
 *         <p/>
 *         corresponts to (7 decimal places on lat and long, directions are NSEW):
 *         <p/>
 *         {"p": { "a" : [
 *         { "t" : 51.4854125, "g" : -0.1217263, "d" : "S", "i" : "At Vauxhall Platform 2"}
 *         ]}}
 */
public class ResultTransformer {

    private static final JsonLiteralValues JV = WmtProperties.SHORTENED_JSON_LITERAL_NAMES ? JsonLiteralValues.SHORT : JsonLiteralValues.LONG;

    public static String toJson(LinkedHashMap<AbstractDirection, List<Point>> points) {
        return makeJsonPoints(convertPoints(points)).toString();
    }

    public static String toJson(SetMultimap<AbstractDirection, Point> points) {
        return makeJsonPoints(points.values()).toString();
    }

    private static StringBuffer makeJsonPoints(Collection<Point> points) {
        StringBuffer buf = new StringBuffer("{" + JV.getPoints() + ": { " + JV.getPointArray() + " : [\n");
        String spaceComma = ", ";
        String spaceColonSpace = " : ";

        for (Point point : points) {
            buf.append("  { ");
            buf.append(JV.getLatitude()).append(spaceColonSpace).append(latLngFormat.format(point.getLat())).append(spaceComma);
            buf.append(JV.getLongitude()).append(spaceColonSpace).append(latLngFormat.format(point.getLng())).append(spaceComma);
            buf.append(JV.getDirection()).append(spaceColonSpace).append(wrapInQuotes(formatDirection(point.getDirection()))).append(spaceComma);
            buf.append(JV.getDescription()).append(spaceColonSpace).append(wrapInQuotes(point.getDescription()));
            buf.append("},\n");
        }
        buf.append("]}}");
        return buf;
    }

    public static enum JsonLiteralValues {
        LONG("points", "pointsArray", "lat", "lng", "direction", "description"),
        SHORT("p", "a", "t", "g", "d", "i");

        private final String points;
        private final String pointArray;
        private final String latitude;
        private final String longitude;
        private final String direction;
        private final String description;

        private JsonLiteralValues(String points, String pointArray, String latitude, String longitude, String direction, String description) {
            this.points = wrapInQuotes(points);
            this.pointArray = wrapInQuotes(pointArray);
            this.latitude = wrapInQuotes(latitude);
            this.longitude = wrapInQuotes(longitude);
            this.direction = wrapInQuotes(direction);
            this.description = wrapInQuotes(description);
        }

        public String getPoints() {
            return points;
        }

        public String getPointArray() {
            return pointArray;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getDirection() {
            return direction;
        }

        public String getDescription() {
            return description;
        }
    }


    private static String wrapInQuotes(String st) {
        return "\"" + st + "\"";
    }


    //8 decimal places for now
    private static final DecimalFormat latLngFormat = new DecimalFormat("#0.00000000");

    private static String formatDirection(Direction direction) {
        return direction.getName().substring(0, 1);
    }

    private static Set<Point> convertPoints(LinkedHashMultimap<AbstractDirection, Point> points) {
        Set<Point> result = new HashSet<Point>();

        for (Point p : points.values()) {
            result.add(p);
        }
        return result;
    }

    private static Set<Point> convertPoints(LinkedHashMap<AbstractDirection, List<Point>> points) {
        Set<Point> result = new HashSet<Point>();

        for (List<Point> ps : points.values()) {
            result.addAll(ps);
        }
        return result;
    }

    private static StringBuffer makeDescriptiveJsonPoints(Set<Point> points) {
        StringBuffer buf = new StringBuffer("{\"points\": { \"pointsArray\" : [\n");

        for (Point point : points) {
            buf.append("  { \"lat\" : " + point.getLat() + ", \"lng\" : " + point.getLng() + ", \"direction\" : \"" + point.getDirection().getName() + "\", \"description\" : \"" + point.getDescription() + "\"},\n");
        }
        buf.append("]}}");
        return buf;
    }
}
