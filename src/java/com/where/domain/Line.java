package com.where.domain;

/**
 * wrapper class
 */
public class Line {
    private final String lineName;

    public Line(String lineName) {
        this.lineName = lineName;
    }

    public String getLineName() {
        return lineName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Line line = (Line) o;

        if (lineName != null ? !lineName.equals(line.lineName) : line.lineName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lineName != null ? lineName.hashCode() : 0;
    }
}
