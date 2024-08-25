package com.sgbd.models.transactions;

import com.sgbd.models.operations.Operation;

import java.util.List;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private List<Operation> operations;

    public Transaction() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }
}
