package com.sgbd.models.transactions;

import com.sgbd.models.locks.Lock;
import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private int id;
    private List<Operation> operations;
    private List<Lock> locks;

    public Transaction(int id) {
        this.id = id;
        this.operations = new ArrayList<>();
        this.locks = new ArrayList<>();
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
    
    public List<Lock> getLocks() {return locks;}
    
    public void setLocks(List<Lock> locks) {}
    
    public void addLock(Lock lock) {
        this.locks.add(lock);
    }

    public List<Lock> getLocksByObj(char objName) {
        List<Lock> objlocks = new ArrayList<>();
        for (Lock lock : locks) {
            if (objName == lock.getOperation().getObject()){
                objlocks.add(lock);
            }
        }
        return objlocks;
    }
}
