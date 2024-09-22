package com.sgbd.models.granularity;

public class Row implements Granularity {
    private final char object;

    public Row(char object) {
        this.object = object;
    }

    public char getObject() {
        return object;
    }
}
