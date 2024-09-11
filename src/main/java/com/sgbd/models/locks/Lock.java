package com.sgbd.models.locks;

import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

public class Lock {
    private LockTypes type;
    private LockStatus status;
    private Operation operation;

    public Lock(Operation operation){
        this.operation = operation;
        this.type = operation.getType() == OperationTypes.COMMIT ? LockTypes.COMMIT
                : operation.getType() == OperationTypes.READ ? LockTypes.READ
                : operation.getType() == OperationTypes.WRITE ? LockTypes.WRITE
                : null;
        this.status = LockStatus.ABORTED;
    }

    public LockStatus getStatus() {
        return status;
    }

    public void setStatus(LockStatus status) {
        this.status = status;
    }

    public LockTypes getType() {
        return type;
    }

    public Operation getOperation() {
        return operation;
    }
}