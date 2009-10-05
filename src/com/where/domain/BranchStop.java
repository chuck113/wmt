package com.where.domain;

public class BranchStop implements java.io.Serializable {

    private final int id;
    private final int orderNo;
    private final Branch branch;
    private final Station station;
    private final TflStationCode tflStationCode;

    public BranchStop(int orderNo, Branch branch, TflStationCode tflStationCode, Station station) {
        this.id = 0;//id;
        this.orderNo = orderNo;
        this.branch = branch;
        this.tflStationCode = tflStationCode;
        this.station = station;
    }

    public TflStationCode getTflStationCode() {
        return tflStationCode;
    }

    public Station getStation() {
        return station;
    }
    
    public Integer getId() {
        return this.id;
    }


    public int getOrderNo() {
        return this.orderNo;
    }


    public Branch getBranch() {
        return this.branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BranchStop stop = (BranchStop) o;

        if (branch != null ? !branch.equals(stop.branch) : stop.branch != null) return false;
        if (station != null ? !station.equals(stop.station) : stop.station != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + orderNo;
        result = 31 * result + (branch != null ? branch.hashCode() : 0);
        result = 31 * result + (station != null ? station.hashCode() : 0);
        result = 31 * result + (tflStationCode != null ? tflStationCode.hashCode() : 0);
        return result;
    }
}