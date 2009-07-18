package com.where.domain;

public class Branch implements java.io.Serializable{
    private final int id;
    private final String name;
    private final String line;

    public Branch(int id, String name, String line) {
        this.name = name;
        this.line = line;
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getLine() {
        return this.line;
    }


}