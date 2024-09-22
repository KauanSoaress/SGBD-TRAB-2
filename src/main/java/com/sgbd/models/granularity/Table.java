package com.sgbd.models.granularity;

import java.util.UUID;

public class Table implements Granularity{
    private final UUID id;
    private final Page pages = new Page(UUID.randomUUID());

    public Table(UUID uuid) {
        id = uuid;
    }

    public UUID getId() {
        return id;
    }

    public Page getPages() {
        return pages;
    }
}
