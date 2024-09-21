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
            } else {
                if (lockTable.grantLock(operation)) {
                    scheduleRegularOperation(operations, operation);
                }
            }
        }
        return 0;
    }

    private void nestedCommitScheduler(List<Operation> operations, Operation commitOperation) {
        List<Number> reachedNodes = new ArrayList<>();

        if (!lockTable.theresWriteOperation(commitOperation.getTransactionId())) {
            if (!lockTable.theresOperationWaiting(commitOperation.getTransactionId())) {
                scheduleRegularOperation(operations, commitOperation);
                lockTable.addCommitGrant(commitOperation);
                lockTable.releaseLocksByTransactionId(commitOperation.getTransactionId());
                reachedNodes = lockTable.waitForGraph.recoverReachedNodes(commitOperation.getTransactionId());
                lockTable.waitForGraph.removeAllEdges(commitOperation.getTransactionId());

                for (Operation op : operations) {
                    if (reachedNodes.contains(op.getTransactionId())) {
                        if (op.getType() == OperationTypes.COMMIT) {
                            nestedCommitScheduler(operations, op);
                        } else if (lockTable.grantLock(op)) {
                            scheduleRegularOperation(operations, op);
                        }
                    }
                }
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        }
        else {
            if (convertWriteToCertify(commitOperation.getTransactionId())) {
                scheduleRegularOperation(operations, commitOperation);
                lockTable.addCommitGrant(commitOperation);
                lockTable.releaseLocksByTransactionId(commitOperation.getTransactionId());
                reachedNodes = lockTable.waitForGraph.recoverReachedNodes(commitOperation.getTransactionId());
                lockTable.waitForGraph.removeAllEdges(commitOperation.getTransactionId());

                for (Operation op : operations) {
                    if (reachedNodes.contains(op.getTransactionId())) {
                        if (op.getType() == OperationTypes.COMMIT) {
                            nestedCommitScheduler(operations, op);
                        } else if (lockTable.grantLock(op)) {
                            scheduleRegularOperation(operations, op);
                        }
                    }
                }
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        }
    }

    public void scheduleRegularOperation(List<Operation> operations, Operation operationToSchedule) {
        scheduledOperations.add(operationToSchedule);
        operations.remove(operationToSchedule);
    }
}