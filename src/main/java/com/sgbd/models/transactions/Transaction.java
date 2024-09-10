package com.sgbd.models.transactions;

import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Transaction {
    private int id;
    private List<Operation> operations;

    public Transaction(int id) {
        this.id = id;
        this.operations = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public void addOperation(Operation operation) {
        this.operations.add(operation);
    }
}
