package com.where.domain;

import com.where.dao.hibernate.Station;

/**
 * */
class PointFactory {
  public static Point make(Station station, Direction direction, String descrption) {
      System.out.println("PointFactory.make lat: "+station.getX()+", lng: "+station.getY());
    return new Point(station.getLat(), station.getLng(), direction, descrption);
  }
}
