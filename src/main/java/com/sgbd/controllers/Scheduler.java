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
            if (lockTable.grantLock(operation)){
                if (operation.getType() == OperationTypes.COMMIT) {

                } else {
                    scheduledOperations.add(operation);
                }
            } else {
            // lockTable.waitForGraph.hasCycle();
            }
        }
        return 0;
    }
}