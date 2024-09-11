package com.sgbd.models.lockTable;

import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operationTypes.OperationTypes;

import java.util.ArrayList;
import java.util.List;

public class LockTable {
    private List<Lock> locks;
    private WaitForGraph waitForGraph;

    public  LockTable(){
        this.locks = new ArrayList<>();
        this.waitForGraph = new WaitForGraph();
    }

    // Lock Conflict Table
    //  ____|wl |rl |cl |
    //  |wl | + | + | - |
    //  |rl | + | - | - |
    //  |cl | - | - | - |
    private int lockConflict(Lock lockOnWait, Lock lockOnGrant) {
        if (lockOnWait.getOperation().getObject() == lockOnGrant.getOperation().getObject()) {
            if (lockOnWait.getType() == lockOnGrant.getType()) {
                if (lockOnWait.getType() == LockTypes.READ) {
                    return -1;
                } else if (lockOnWait.getType() == LockTypes.WRITE) {
                    return lockOnGrant.getOperation().getTransactionId();
                } else if (lockOnWait.getType() == LockTypes.COMMIT) {
                    return lockOnGrant.getOperation().getTransactionId();
                }
            } else if (lockOnWait.getType() == LockTypes.COMMIT || lockOnGrant.getType() == LockTypes.COMMIT) {
                return lockOnGrant.getOperation().getTransactionId();
            } else if (lockOnWait.getType() == LockTypes.READ || lockOnGrant.getType() == LockTypes.READ) {
                return -1;
            }
        }
        return -1;
    }

    public int addLock(Lock lock){
        int conflictingTransaction = -1;
        for (Lock l: locks){
            conflictingTransaction = lockConflict(lock, l);
            System.out.println("conflictingTransaction: " + conflictingTransaction);
            if (conflictingTransaction != -1){
                waitForGraph.addWaitEdge(lock.getOperation().getTransactionId(), conflictingTransaction);
                lock.setStatus(LockStatus.WAITING);
            }
            if (waitForGraph.hasCycle()){
                return 1;
            }
        }
        lock.setStatus(LockStatus.GRANTED);
        locks.add(lock);
        System.out.println("Lock granted");
        return 0;
    }
}


