package com.sgbd.models.lockTable;

import com.sgbd.models.granularity.Row;
import com.sgbd.models.granularity.Table;
import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LockTable {
    public final List<Lock> locks;
    public WaitForGraph waitForGraph;
    public Table table = new Table(UUID.randomUUID());

    private static final boolean[][] conflictTable = {
            // Granted Locks:       rl       wl     ul      cl      irl     iwl     iul   icl
            /* Requested rl */    { false,  false,  true,   true,   false,  true,   true, true },
            /* Requested wl */    { false,  true,   true,   true,   true,   true,   true, true },
            /* Requested ul */    { false,  true,   true,   true,   false,  true,   true, true },
            /* Requested cl */    { true,   true,   true,   true,   true,   true,   true, true },
            /* Requested irl */   { false,  true,   true,   true,   false,  false,  false, false },
            /* Requested iwl */   { true,   true,   true,   true,   false,  false,  false, false },
            /* Requested iul */   { false,  true,   true,   true,   false,  false,  false, false },
            /* Requested icl */   { true,   true,   true,   true,   false,  false,  false, false }
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
            case CERTIFY_INTENT -> 7;
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
        Row row = new Row(operation.getObject());
        table.getPages().getRows().add(row);

//        Lock lockTable = new Lock(operation, table);
//      IMPLEMENTAR A LÓGICA DE CONCEDER OS BLOQUEIOS INTENCIONAIS PARA CIMA

        Lock lock = new Lock(operation);
        List<Lock> grantedLocks = locks.stream().filter(lk -> lk.getStatus().equals(LockStatus.GRANTED)).toList();

        if (!locks.isEmpty()) {
            for (Lock l : grantedLocks) {
                if (lockConflict(lock, l)) {
                    lock.setStatus(LockStatus.WAITING);
                    locks.add(lock);
                    waitForGraph.addWaitEdge(l.getTransactionId(), lock.getTransactionId());
                    return false;
                }
            }
        }
        lock.setStatus(LockStatus.GRANTED);
        locks.add(lock);
        return true;
    }

    public void addCommitGrant(Operation operation) {
        Lock lock = new Lock(operation);
        lock.setStatus(LockStatus.GRANTED);
        locks.add(lock);
    }

    public void addCommitWait(Operation operation) {
        Lock lock = new Lock(operation);
        lock.setStatus(LockStatus.WAITING);
        locks.add(lock);
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

    public boolean convertWriteToCertify(int transactionId) {
        if (canConvertWriteToCertify(transactionId)) {
            locks.stream()
                    .filter(lk -> lk.getType().equals(LockTypes.WRITE))
                    .forEach(lk -> lk.setType(LockTypes.CERTIFY));
            return true;
        }
        return false;
    }

    private boolean canConvertWriteToCertify(Integer transactionId) {
        Set<Object> writeLockedObjects = locks.stream()
                .filter(lock -> lock.getType().equals(LockTypes.WRITE) && lock.getTransactionId().equals(transactionId))
                .map(Lock::getObject)
                .collect(Collectors.toSet());

        return locks.stream()
                .filter(lock -> lock.getType().equals(LockTypes.READ) && !lock.getTransactionId().equals(transactionId))
                .noneMatch(lock -> writeLockedObjects.contains(lock.getObject()));
    }

    public boolean canGrantLock(Lock currentLock) {
        List<Lock> grantedLocks = locks.stream().filter(lk -> lk.getStatus().equals(LockStatus.GRANTED)).toList();
        List<Lock> sameTransactionLocks = locks
                .stream()
                .filter(lock -> lock.getTransactionId()
                        .equals(currentLock.getTransactionId()) &&
                        lock.getStatus().equals(LockStatus.WAITING)
                )
                .toList();

        if (!sameTransactionLocks.get(0).equals(currentLock)) {
            return false;
        }

        for (Lock lock: grantedLocks) {
            if (lockConflict(currentLock, lock)) {
                waitForGraph.addWaitEdge(lock.getTransactionId(), currentLock.getTransactionId());
                return false;
            }
        }
        return true;
    }
}