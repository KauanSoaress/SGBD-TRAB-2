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

    private void nestedCommitScheduler(List<Operation> operations, Operation commitOperation) {
        List<Number> reachedNodes = new ArrayList<>();

        if (!lockTable.theresWriteOperation(commitOperation.getTransactionId())) {
            if (!lockTable.theresOperationWaiting(commitOperation.getTransactionId())) {
                scheduledOperations.add(commitOperation);
                lockTable.addCommitGrant(commitOperation);
                lockTable.releaseLocksByTransactionId(commitOperation.getTransactionId());
                reachedNodes = lockTable.waitForGraph.recoverReachedNodes(commitOperation.getTransactionId());

                for (Operation op : operations) {
                    if (reachedNodes.contains(op.getTransactionId())) {
                        if (op.getType() == OperationTypes.COMMIT) {
                            nestedCommitScheduler(operations, op);
                        } else if (lockTable.grantLock(op)) {
                            scheduledOperations.add(op);
                        }
                    }
                }
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        }
        else {
            // Adicionar esquema para conversão para certify lock, caso seja possível, se não, aguardar
        }
    }
}