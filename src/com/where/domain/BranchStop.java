package com.where.domain;

public class BranchStop implements java.io.Serializable {

    private final int id;
    private final int orderNo;
    private final Branch branch;
    private final Station station;
    private final TflStationCode tflStationCode;

    public BranchStop(int id, int orderNo, Branch branch, TflStationCode tflStationCode, Station station) {
        this.id = id;
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
    public String toString() {
        return "BranchStop[id: '"+getId()+"', name: '"+this.station.getName()+"', branch: '"+getBranch().getName()+"']";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BranchStop stop = (BranchStop) o;

        if (id != stop.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}