package com.where.domain;

import java.io.Serializable;

/**
 * Represents the a train on a map
 */
public class Point implements Serializable {
    private final double lat;
    private final double lng;
    private final Direction direction;
    private final String description;

    public Point(double x, double y, Direction direction, String description) {
        this.lng = y;
        this.lat = x;
        this.direction = direction;
        this.description = description;
    }


    public Direction getDirection() {
        return direction;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDescription() {
        return description;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        return Double.compare(point.lat, lat) == 0 && Double.compare(point.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lng != +0.0d ? Double.doubleToLongBits(lng) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Point{" +                
                ", lat=" + lat +
                ", lng=" + lng +
                ", direction=" + direction +
                ", description='" + description + '\'' +
                '}';
    }
}
