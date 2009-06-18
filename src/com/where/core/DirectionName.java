package com.where.core;

/**
 * */
enum DirectionName {

  NORTHBOUND("Northbound"),
  SOUTHBOUND("Southbound"),
  EASTBOUND("Eastbound"),
  WESTBOUND("Westbound");

  private final String name;

  DirectionName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
