package com.sgbd.controllers;

import com.sgbd.models.granularity.GranularityType;
import com.sgbd.models.lockTable.LockTable;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;
import com.sgbd.models.operations.OperationStatus;

import java.util.*;

public class Scheduler {
    private final LockTable lockTable;
    private List<Operation> scheduledOperations = new ArrayList<>();
    private GranularityType granularityType = GranularityType.ROW;

    public Scheduler(List<Operation> operations) {
        lockTable = new LockTable(operations, scheduledOperations);
    }

    public int schedule(List<Operation> operations) {
        for (Operation operation : operations) {
            System.out.println("=====================================");
            System.out.println("Transação da operação: " + operation.getTransactionId() + " \ntipo da op: " + operation.getType() + " \nobjeto da op " + operation.getObject());
            if (lockTable.abortedTransactions.isEmpty() || !lockTable.abortedTransactions.contains(operation.getTransactionId())) {
                if (operation.getType() == OperationTypes.COMMIT) {
                    nestedCommitScheduler(operations, operation);
                    System.out.println("Bloqueio concedido para a operação: " + operation.getTransactionId() + " " + operation.getType() + " " + operation.getObject());
                } else {
                    if (lockTable.grantLock(operation)) {
                        scheduleRegularOperation(operations, operation);
                        System.out.println("Bloqueio concedido para a operação: " + operation.getTransactionId() + " " + operation.getType() + " " + operation.getObject());
                    }
                }
            }
            System.out.println("=====================================");
        }

        return 0;
    }

    private void nestedCommitScheduler(List<Operation> operations, Operation commitOperation) {
        /*
            * Se não houver bloqueio de escrita para o commit, e não houver bloqueio em espera para o commit,
            * então o commit pode ser escalonado.
            * Se houver bloqueio de escrita para o commit, e for possível converter o bloqueio de escrita em certify,
            * então o commit pode ser escalonado.
            * Caso contrário, o commit deve ser colocado em espera.
         */
        if (!lockTable.theresWriteOperation(commitOperation.getTransactionId())) {
            if (!lockTable.theresOperationWaiting(commitOperation.getTransactionId())) {
                scheduleCommitOperation(operations, commitOperation);
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        } else {
            if (lockTable.convertWriteToCertify(commitOperation.getTransactionId())) {
                scheduleCommitOperation(operations, commitOperation);
            } else {
                lockTable.addCommitWait(commitOperation);
            }
        }
    }

    private void scheduleCommitOperation(List<Operation> operations, Operation commitOperation){
        scheduleRegularOperation(operations, commitOperation);
        lockTable.addCommitGrant(commitOperation);
        lockTable.releaseLocksByTransactionId(commitOperation.getTransactionId());

        List<Integer> reachedNodes = lockTable.waitForGraph.recoverReachedNodes(commitOperation.getTransactionId());
        lockTable.waitForGraph.removeAllEdges(commitOperation.getTransactionId());

        /*
            * Para cada nó relacionado a transação desse commit,
            * se houver bloqueio em espera para o nó, e for possível conceder o bloqueio,
            * então o bloqueio é concedido.
            * Caso contrário, o bloqueio é mantido em espera.
         */
        for (Integer transactionId : reachedNodes) {
            lockTable.locks.stream()
                .filter(lock -> lock != null && lock.getTransactionId().equals(transactionId) && lock.getStatus().equals(LockStatus.WAITING))
                .peek(lock -> {
                    lockTable.removeLock(lock);
                    if (lockTable.grantLock(lock.getOperation())){
                        scheduleRegularOperation(operations, lock.getOperation());
                    }
                });
        }
//        Optional.ofNullable(reachedNodes)
//            .ifPresent(nodes -> nodes.forEach(transactionId -> {
//                lockTable.locks.stream()
//                    .filter(lock -> lock != null && lock.getTransactionId().equals(transactionId) && lock.getStatus().equals(LockStatus.WAITING))
//                    .peek(lock -> {
//                        lockTable.removeLock(lock);
//                        lockTable.grantLock(lock.getOperation());
//
//                    });
//            }));
    }

    public void scheduleRegularOperation(List<Operation> operations, Operation operationToSchedule) {
        if (operationToSchedule.getStatus().equals(OperationStatus.NONEXECUTED)) {
            operationToSchedule.setStatus(OperationStatus.EXECUTED);
            scheduledOperations.add(operationToSchedule);
        }
    }

    public List<Operation> getScheduledOperations() {
        return scheduledOperations;
    }

}