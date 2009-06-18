package com.where.core;

import com.where.dao.hibernate.Station;

/**
 * */
class PointFactory {
  public static Point make(Station station) {
    return new Point(station.getX(), station.getY());
  }
}
