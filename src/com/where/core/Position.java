package com.where.core;

import com.where.dao.hibernate.BranchStop;

/**
 * */
public class Position {
  private final BranchStop start;
  private final BranchStop end;

  public Position(BranchStop start, BranchStop end) {
    this.start = start;
    this.end = end;
  }

  public BranchStop findFurthest() {
    if (start == null && end == null) {
      return null;
    } else if (start == null && end != null) {
      return end;
    } else {
      return start;
    }
  }

  public Point makeMidwayPoint() {
    if (start == null && end != null) {
      return PointFactory.make(end.getStation());
    } else if (start != null && end == null) {
      return PointFactory.make(start.getStation());
    } else if (start != null && end != null) {
      return new Point((end.getStation().getX() + start.getStation().getY()) / 2, (start.getStation().getY() + end.getStation().getY()) / 2);
    }
    return null;
  }
}
