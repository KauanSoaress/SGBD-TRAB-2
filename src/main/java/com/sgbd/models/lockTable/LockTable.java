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

    private static final boolean[][] conflictTable = {
            // Granted Locks:       rl       wl       ul       cl       irl      iwl      iul
            /* Requested rl */    { false,  false,  true,   true,   false,  true,   true },
            /* Requested wl */    { false,  true,   true,   true,   true,   true,   true },
            /* Requested ul */    { false,  true,   true,   true,   false,  true,   true },
            /* Requested cl */    { true,   true,   true,   true,   true,   true,   true },
            /* Requested irl */   { false,  true,   true,   true,   false,  false,  false },
            /* Requested iwl */   { true,   true,   true,   true,   false,  false,  false },
            /* Requested iul */   { false,  true,   true,   true,   false,  false,  false }
    };

    public  LockTable(){
        this.locks = new ArrayList<>();
        this.waitForGraph = new WaitForGraph();
    }

    private int getLockTypeIndex(LockTypes type) {
        return switch (type) {
            case READ -> 0;
            case WRITE -> 1;
            case UPDATE -> 2;
            case CERTIFY -> 3;
            case READ_INTENT -> 4;
            case WRITE_INTENT -> 5;
            case UPDATE_INTENT -> 6;
            default -> throw new IllegalArgumentException("Tipo de bloqueio desconhecido: " + type);
        };
    }

    // Lock Conflict Table
    //  ____|rl |wl |ul |cl |irl|iwl|iul|
    //  |rl | + | + | - | - | + | - | - |
    //  |wl | + | - | - | - | - | - | - |
    //  |ul | + | - | - | - | + | - | - |
    //  |cl | - | - | - | - | - | - | - |
    //  |irl| + | - | - | - | + | + | + |
    //  |iwl| - | - | - | - | + | + | + |
    //  |iul| + | - | - | - | + | + | + |

    private boolean lockConflict(Lock lockOnWait, Lock lockOnGrant) {
        if (lockOnWait.getObject() == lockOnGrant.getObject()) {
            int requestedIndex = getLockTypeIndex(lockOnWait.getType());
            int grantedIndex = getLockTypeIndex(lockOnGrant.getType());
            return conflictTable[requestedIndex][grantedIndex];
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
}


