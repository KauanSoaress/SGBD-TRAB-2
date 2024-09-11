package com.sgbd.controllers;

import com.sgbd.models.lockTable.LockTable;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.operations.Operation;
import com.sgbd.models.transactions.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scheduler {
    private LockTable lockTable;

    public Scheduler() {
        lockTable = new LockTable();
    }

    public int schedule(List<Operation> operations) {
        Lock currentLock;
        for (Operation operation : operations) {
            currentLock = new Lock(operation);
            if (lockTable.addLock(currentLock) == 1) {
                System.out.println("Deadlock detected");
                return 1;
            }
        }
        return 0;
    }

    private void updateSyslockinfo() {
        // Implementar lógica para atualizar a cópia do syslockinfo
    }

    private void updateWaitForGraph() {
        // Implementar lógica para atualizar o grafo de espera e procurar por ciclos
    }

    private void preventDeadlock() {
        // Implementar lógica para prevenir deadlock
    }
}