package com.where.domain;

/**
 * @author Charles Kubicek
 */
public interface Direction {

    String getName();

    public enum DirectionImpl implements Direction {

        NORTHBOUND("Northbound"),
        SOUTHBOUND("Southbound"),
        EASTBOUND("Eastbound"),
        WESTBOUND("Westbound");

        private final String name;
        public static final Direction NULL_DIRECTION = new NullDirection();

        DirectionImpl(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
