package com.sgbd.models.lockTable;

import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;

public class LockTable {
    private final List<Lock> locks;
    public WaitForGraph waitForGraph;

    public  LockTable(){
        this.locks = new ArrayList<>();
        this.waitForGraph = new WaitForGraph();
    }

    // Lock Conflict Table
    //  ____|wl |rl |cl |
    //  |wl | + | + | - |
    //  |rl | + | - | - |
    //  |cl | - | - | - |

    private boolean lockConflict(Lock lockOnWait, Lock lockOnGrant) {
        if (lockOnWait.getObject() == lockOnGrant.getObject()) {
            if (lockOnWait.getType() == lockOnGrant.getType()) {
                return lockOnWait.getType() != LockTypes.READ;
            } else if (lockOnWait.getType() == LockTypes.CERTIFY || lockOnGrant.getType() == LockTypes.CERTIFY) {
                return true;
            } else if (lockOnWait.getType() == LockTypes.READ || lockOnGrant.getType() == LockTypes.READ) {
                return false;
            }
        }
        return false;
    }

    public boolean grantLock(Operation operation) {
        Lock lock = new Lock(operation);

        if (!locks.isEmpty()) {
            for (Lock l : locks) {
                if (lockConflict(lock, l)) {
                    lock.setStatus(LockStatus.WAITING);
                    locks.add(lock);
                    waitForGraph.addWaitEdge(lock.getTransactionId(), l.getTransactionId());
                    return false;
                }
            }
        }
        lock.setStatus(LockStatus.GRANTED);
        locks.add(lock);
        return true;
    }

    public boolean theresOperationWaiting(int transactionId) {
        return locks.stream().anyMatch(lock -> lock.getTransactionId() == transactionId && lock.getStatus() == LockStatus.WAITING);
    }

    public boolean theresWriteOperation(int transactionId) {
        return locks.stream().anyMatch(lock -> lock.getTransactionId() == transactionId && lock.getType() == LockTypes.WRITE);
    }

    public void releaseLocksByTransactionId(int transactionId) {
        locks.removeIf(lock -> lock.getTransactionId() == transactionId);
    }
}


