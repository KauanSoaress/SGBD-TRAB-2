package com.sgbd.controllers;

import com.sgbd.models.lockTable.LockTable;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

import java.util.*;

public class Scheduler {
    private final LockTable lockTable;
    private final List<Operation> scheduledOperations = new ArrayList<>();

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
        Map<Integer, List<Lock>> locks = new HashMap<>();

        if (!lockTable.theresWriteOperation(commitOperation.getTransactionId())) {
            if (!lockTable.theresOperationWaiting(commitOperation.getTransactionId())) {
                scheduleCommitOperation(operations, commitOperation, locks);
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        } else {
            if (lockTable.convertWriteToCertify(commitOperation.getTransactionId())) {
                scheduleCommitOperation(operations, commitOperation, locks);
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        }
    }

    private void scheduleCommitOperation(List<Operation> operations, Operation commitOperation, Map<Integer, List<Lock>> locks) {
        scheduleRegularOperation(operations, commitOperation);
        lockTable.addCommitGrant(commitOperation);
        lockTable.releaseLocksByTransactionId(commitOperation.getTransactionId());

        List<Integer> reachedNodes = lockTable.waitForGraph.recoverReachedNodes(commitOperation.getTransactionId());
        lockTable.waitForGraph.removeAllEdges(commitOperation.getTransactionId());

        Optional.ofNullable(reachedNodes)
            .ifPresent(nodes -> nodes.forEach(transactionId -> {
                List<Lock> waitingLocks = lockTable.locks.stream()
                    .filter(lock -> lock.getTransactionId().equals(transactionId) && lock.getStatus().equals(LockStatus.WAITING))
                    .peek(lock -> {
                        if (lockTable.canGrantLock(lock)) {
                            lock.setStatus(LockStatus.GRANTED);
                        }
                    })
                    .toList();
                locks.put(transactionId, waitingLocks);
            }));
    }

    public void scheduleRegularOperation(List<Operation> operations, Operation operationToSchedule) {
        scheduledOperations.add(operationToSchedule);
        operations.remove(operationToSchedule);
    }
}