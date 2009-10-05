package com.where.domain;

import com.where.hibernate.Station;

/**
 * */
class PointFactory {
  public static Point make(Station station, Direction direction, String descrption) {
    return new Point(station.getLat(), station.getLng(), direction, descrption);
  }
}
