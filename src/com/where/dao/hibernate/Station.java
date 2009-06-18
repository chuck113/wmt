package com.where.dao.hibernate;
// Generated 24-Aug-2008 23:56:26 by Hibernate Tools 3.2.1.GA


/**
 * Stations generated by hbm2java
 */
public class Station implements java.io.Serializable {

  private Integer id;
  private String name;
  private String line;
  private double x;
  private double y;

  public Station() {
  }

  public Station(String name, String line, double x, double y) {
    this.name = name;
    this.line = line;
    this.x = x;
    this.y = y;
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLine() {
    return this.line;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public double getX() {
    return this.x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return this.y;
  }

  public void setY(double y) {
    this.y = y;
  }
}


