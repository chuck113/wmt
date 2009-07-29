package com.where.domain;

/**
 * @author Charles Kubicek
 */
public interface Direction {

    String getName();

    public static final Direction NULL_DIRECTION = new NullDirection();

    public enum DirectionEnum implements Direction {

        NORTHBOUND("Northbound"),
        SOUTHBOUND("Southbound"),
        EASTBOUND("Eastbound"),
        WESTBOUND("Westbound");

        private final String name;


        DirectionEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class NullDirection implements Direction{
        public String getName() {
            return "NULL";
        }
    }
}
