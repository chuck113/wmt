package com.where.dao.hsqldb;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeInfo timeInfo = (TimeInfo) o;

        if (info != null ? !info.equals(timeInfo.info) : timeInfo.info != null) return false;
        if (time != null ? !time.equals(timeInfo.time) : timeInfo.time != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = info != null ? info.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }
}
