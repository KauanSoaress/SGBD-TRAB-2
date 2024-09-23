package com.sgbd.models.locks;

import com.sgbd.models.granularity.Granularity;
import com.sgbd.models.granularity.GranularityType;
import com.sgbd.models.granularity.Row;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

public class Lock {
    private LockTypes type;
    private LockStatus status;
    private final Operation operation;
    private final Integer transactionId;
    private final char object;
    private final Granularity granularity;
    private final GranularityType granularityType;

    public Lock(Operation operation, Granularity granularity){
        this.operation = operation;
        this.type = determineLockType(operation.getType());
        this.status = LockStatus.NOT_GRANTED;
        this.transactionId = operation.getTransactionId();
        this.object = operation.getObject();
        this.granularity = granularity;
        this.granularityType = granularity.getGranularityType();
    }

    public Lock(Operation operation, LockTypes type, Granularity granularity){
        this.operation = operation;
        this.type = type;
        this.status = LockStatus.NOT_GRANTED;
        this.transactionId = operation.getTransactionId();
        this.object = operation.getObject();
        this.granularity = granularity;
        this.granularityType = granularity.getGranularityType();
    }

    private LockTypes determineLockType(OperationTypes operationType) {
        return switch (operationType) {
            case COMMIT -> LockTypes.COMMIT;
            case UPDATE -> LockTypes.UPDATE;
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

    public Granularity getGranularity() {
        return granularity;
    }

    public GranularityType getGranularityType() {
        return granularityType;
    }
}