package com.sgbd.models.operations;

import com.sgbd.models.operationTypes.OperationTypes;

import java.util.Date;
import java.util.UUID;

public class Operation {
    private UUID id;
    private Date timestamp;
    private OperationTypes type;
    private String object;

    public Operation(OperationTypes type) {
        this.id = UUID.randomUUID();
        this.timestamp = new Date();
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public OperationTypes getType() {
        return type;
    }

    public void setType(OperationTypes type) {
        this.type = type;
    }
}
