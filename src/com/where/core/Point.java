package com.where.core;

/**
 * */
class Point {
  private final double x;
  private final double y;

  public Point(double x, double y) {
    this.y = y;
    this.x = x;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;

    return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
  }
}
