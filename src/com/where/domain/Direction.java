package com.where.domain;

/**
 * */
public enum Direction {

  NORTHBOUND("Northbound"),
  SOUTHBOUND("Southbound"),
  EASTBOUND("Eastbound"),
  WESTBOUND("Westbound");

  private final String name;

  Direction(String name) {
    this.name = name;
  }

    public String getName() {
    return name;
  }
}
