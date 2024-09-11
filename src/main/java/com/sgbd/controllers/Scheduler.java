package com.sgbd.controllers;

import com.sgbd.models.lockTable.LockTable;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.operations.Operation;
import com.sgbd.models.transactions.Transaction;

import java.util.*;

public class Scheduler {
    private LockTable lockTable;
    private List<Transaction> transactions;
    private Set<Integer> transactionsIds;
//editado no pc do PET
    public Scheduler() {
        lockTable = new LockTable();
        transactionsIds = new HashSet<>();
        transactions = new ArrayList<>();
    }

    public int schedule(List<Operation> operations) {
        Lock currentLock;
        Transaction newTransaction = null;

        for (Operation operation : operations) {
            if (!transactionsIds.contains(operation.getTransactionId())) {
                newTransaction = new Transaction(operation.getTransactionId());
                newTransaction.addOperation(operation);
                transactions.add(newTransaction);
                transactionsIds.add(operation.getTransactionId());
                newTransaction = null;
            }
            else {
                for (Transaction transaction : transactions) {
                    if (transaction.getId() == operation.getTransactionId()) {
                        transaction.addOperation(operation);
                        break;
                    }
                }
            }
            currentLock = new Lock(operation);
            if (lockTable.grantLock(currentLock) == 1) {
                System.out.println("Deadlock detected");
                return 1;
            }
        }



        return 0;
    }

    private void updateSyslockinfo() {
        // Implementar lógica para atualizar a cópia do syslockinfo
    }
}