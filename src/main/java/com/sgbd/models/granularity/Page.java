package com.sgbd.models.granularity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Page extends Granularity {
    private final UUID id;
    private List<Row> rows;
    private final GranularityType granularityType = GranularityType.PAGE;

    public Page(UUID uuid) {
        id = uuid;
        rows = new ArrayList<Row>();
    }

    public UUID getId() {
        return id;
    }

    public List<Row> getRows() {
        return rows;
    }

    public GranularityType getGranularityType() {
        return granularityType;
    }
}
