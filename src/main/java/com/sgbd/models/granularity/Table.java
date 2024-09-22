package com.sgbd.models.granularity;

import java.util.UUID;

public class Table extends Granularity {
    private final UUID id;
    private final Page pages = new Page(UUID.randomUUID());
    private final GranularityType granularityType = GranularityType.TABLE;

    public Table(UUID uuid) {
        id = uuid;
    }

    public UUID getId() {
        return id;
    }

    public Page getPages() {
        return pages;
    }

    public GranularityType getGranularityType() {
        return granularityType;
    }
}
