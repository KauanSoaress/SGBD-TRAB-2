package com.sgbd.models.granularity;

public class Row extends Granularity {
    private final char object;
    private final GranularityType granularityType = GranularityType.ROW;

    public Row(char object) {
        this.object = object;
    }

    public char getObject() {
        return object;
    }

    public GranularityType getGranularityType() {
        return granularityType;
    }
}
