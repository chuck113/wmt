package com.where.hibernate;
// Generated 24-Aug-2008 23:56:26 by Hibernate Tools 3.2.1.GA


/**
 * BranchStops generated by hbm2java
 */
public class BranchStop implements java.io.Serializable {

  private Integer id;
  private String stationId;
  private int orderNo;
  private String branchId;

  //CK
  private Station station;
  private TflStationCode stationCode;

  public TflStationCode getStationCode() {
    return stationCode;
  }

  public void setStationCode(TflStationCode stationCode) {
    this.stationCode = stationCode;
  }

  public Station getStation() {
    return station;
  }

  public void setStation(Station station) {
    this.station = station;
  }

  public BranchStop() {
  }

  public BranchStop(String stationId, int orderNo, String branchId) {
    this.stationId = stationId;
    this.orderNo = orderNo;
    this.branchId = branchId;
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getStationId() {
    return this.stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public int getOrderNo() {
    return this.orderNo;
  }

  public void setOrderNo(int orderNo) {
    this.orderNo = orderNo;
  }

  public String getBranchId() {
    return this.branchId;
  }

  public void setBranchId(String branchId) {
    this.branchId = branchId;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BranchStop that = (BranchStop) o;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;

    return true;
  }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        return result;
    }
}


