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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Branch branch = (Branch) o;

        if (line != null ? !line.equals(branch.line) : branch.line != null) return false;
        if (name != null ? !name.equals(branch.name) : branch.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (line != null ? line.hashCode() : 0);
        return result;
    }
}