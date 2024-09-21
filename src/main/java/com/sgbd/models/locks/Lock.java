package com.sgbd.models.locks;

import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

public class Lock {
    private LockTypes type;
    private LockStatus status;
    private final Operation operation;
    private final Integer transactionId;
    private final char object;

    public Lock(Operation operation){
        this.operation = operation;
        this.type = determineLockType(operation.getType());
        this.status = LockStatus.NOT_GRANTED;
        this.transactionId = operation.getTransactionId();
        this.object = operation.getObject();
    }

    private LockTypes determineLockType(OperationTypes operationType) {
        return switch (operationType) {
            case COMMIT -> LockTypes.CERTIFY;
            case READ -> LockTypes.READ;
            case WRITE -> LockTypes.WRITE;
        };
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

    public void certifyLock(){
        type = LockTypes.CERTIFY;
    }

    public void setType(LockTypes type) {
        this.type = type;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public char getObject() {
        return object;
    }
}