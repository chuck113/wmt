package com.where.dao;

/**
 * */
public class TimeInfo {
  private final String info;
  private final String time;

  public TimeInfo(String time, String info) {
    this.info = info;
    this.time = time;
  }

  public String getInfo() {
    return info;
  }

  public String getTime() {
    return time;
  }


  public String toString() {
    return "TimeInfo{" +
            "info='" + info + '\'' +
            ", time='" + time + '\'' +
            '}';
  }
}
