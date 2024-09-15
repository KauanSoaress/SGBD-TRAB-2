package com.sgbd.controllers;

import com.sgbd.models.lockTable.LockTable;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private final LockTable lockTable;
    private List<Operation> scheduledOperations = new ArrayList<>();

    public Scheduler() {
        lockTable = new LockTable();
    }

    public int schedule(List<Operation> operations) {
        for (Operation operation : operations) {
            if (operation.getType() == OperationTypes.COMMIT) {
                nestedCommitScheduler(operations, operation);
            }

            else if (lockTable.grantLock(operation)) {

            }
//            if (lockTable.grantLock(operation)){
//                if (operation.getType() == OperationTypes.COMMIT) {
//
//                } else {
//                    scheduledOperations.add(operation);
//                }
//            } else {
//            // lockTable.waitForGraph.hasCycle();
//            }
        }
        return 0;
    }

    private void nestedCommitScheduler(List<Operation> operations, Operation operation) {
        List<Number> reachedNodes = new ArrayList<>();
        boolean theresWriteOperation = false;
        for (Operation operationToCheck: operations) {
            if (operationToCheck.getTransactionId().equals(operation.getTransactionId())) {
                if (operationToCheck.getType() == OperationTypes.WRITE) {
                    theresWriteOperation = true;
                    break;
                }
            }
        }

        if (!theresWriteOperation) {
            scheduledOperations.add(operation);
            lockTable.releaseLocksByTransactionId(operation.getTransactionId());
            reachedNodes = lockTable.waitForGraph.recoverReachedNodes(operation.getTransactionId());

            for (Operation op : operations) {
                if (reachedNodes.contains(op.getTransactionId())) {
                    if (op.getType() == OperationTypes.COMMIT) {
                        nestedCommitScheduler(operations, op);
                    } else if (lockTable.grantLock(op)) {
                        scheduledOperations.add(op);
                    }
                }
            }
        }
        else {
            // Adicionar esquema para conversão para certify lock, caso seja possível, se não, aguardar
        }
    }
}