package com.sgbd.models.operations;

import com.sgbd.models.operationTypes.OperationTypes;

import java.util.Date;
import java.util.UUID;

public class Operation {
    private UUID id;
    private Date timestamp;
    private OperationTypes type;
    private final int transactionId;
    private char object;

    public Operation(OperationTypes type, int transactionId, char object) {
        this.id = UUID.randomUUID();
        this.timestamp = new Date();
        this.type = type;
        this.transactionId = transactionId;
        this.object = object;
    }

    public Operation(OperationTypes operationTypes, int transactionId) {
        this.id = UUID.randomUUID();
        this.timestamp = new Date();
        this.type = operationTypes;
        this.transactionId = transactionId;
    }

    public char getObject() {
        return object;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setObject(char object) {
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
