package com.sgbd.models.lockTable;

import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LockTable {
    public final List<Lock> locks;
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

        if (!sameTransactionLocks.getFirst().equals(currentLock)) {
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