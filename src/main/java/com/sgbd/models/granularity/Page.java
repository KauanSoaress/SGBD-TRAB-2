package com.sgbd.models.granularity;

import java.util.List;
import java.util.UUID;

public class Page implements Granularity {
    private final UUID id;
    private List<Row> rows;

    public Page(UUID uuid) {
        id = uuid;
    }

    public UUID getId() {
        return id;
    }

    public List<Row> getRows() {
        return rows;
    }
}
