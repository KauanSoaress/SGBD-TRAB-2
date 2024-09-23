package com.sgbd.models.lockTable;

import com.sgbd.models.granularity.GranularityType;
import com.sgbd.models.granularity.Row;
import com.sgbd.models.granularity.Table;
import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operations.Operation;
import com.sgbd.models.operations.OperationStatus;

import java.util.*;
import java.util.stream.Collectors;

public class LockTable {
    public final List<Lock> locks;
    public WaitForGraph waitForGraph;
    public Table table = new Table(UUID.randomUUID());
    public List<Integer> abortedTransactions = new ArrayList<>();
    public List<Operation> operations;
    public List<Operation> scheduledOperations;

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

    public LockTable(List<Operation> operations, List<Operation> scheduledOperations) {
        this.locks = new ArrayList<>();
        this.waitForGraph = new WaitForGraph();
        this.operations = operations;
        this.scheduledOperations = scheduledOperations;
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

    private Row findRow(char object) {
        Row objectRow = table
                .getPages()
                .getRows()
                .stream()
                .filter(row -> row.getObject() == object)
                .findFirst()
                .orElse(null);

        if (objectRow == null) {
            objectRow = new Row(object);
            table.getPages().getRows().add(objectRow);
        }

        return objectRow;
    }

    public boolean grantLock(Operation operation) {
        Row row = findRow(operation.getObject());
        Lock lock = new Lock(operation, row);

        List<Lock> pageLocks = locks.stream()
            .filter(lk -> lk.getGranularityType().equals(GranularityType.PAGE))
            .toList();

        for (Lock pageLock : pageLocks) {
            if (lockConflict(lock, pageLock)) {
                lock.setStatus(LockStatus.WAITING);
                locks.add(lock);
                addEdgeAndVerifyDeadlock(pageLock.getTransactionId(), lock.getTransactionId());
                return false;
            }
        }

        Lock currentRowLock = locks.stream()
            .filter(lk -> lk.getGranularity().equals(row) && lk.getStatus().equals(LockStatus.GRANTED))
            .findFirst()
            .orElse(null);

        if (currentRowLock != null && lockConflict(lock, currentRowLock)) {
            lock.setStatus(LockStatus.WAITING);
            locks.add(lock);
            addEdgeAndVerifyDeadlock(currentRowLock.getTransactionId(), lock.getTransactionId());
            return false;
        }

        Lock firstTransactionWaitingLock = locks.stream()
            .filter(lk -> lk.getTransactionId().equals(operation.getTransactionId()) && lk.getStatus().equals(LockStatus.WAITING))
            .findFirst()
            .orElse(null);

        if (firstTransactionWaitingLock != null && !firstTransactionWaitingLock.equals(lock)) {
            lock.setStatus(LockStatus.WAITING);
            locks.add(lock);
            return false;
        }

        Lock tableIntentLock = new Lock(operation, determineLockType(operation), table);
        Lock pageIntentLock = new Lock(operation, determineLockType(operation), table.getPages());

        tableIntentLock.setStatus(LockStatus.GRANTED);
        pageIntentLock.setStatus(LockStatus.GRANTED);

        locks.add(tableIntentLock);
        locks.add(pageIntentLock);

        lock.setStatus(LockStatus.GRANTED);
        locks.add(lock);
        return true;
    }

    public LockTypes determineLockType(Operation operation) {
        return switch (operation.getType()) {
            case WRITE -> LockTypes.WRITE_INTENT;
            case READ -> LockTypes.READ_INTENT;
            case UPDATE -> LockTypes.UPDATE_INTENT;
            case COMMIT -> LockTypes.CERTIFY_INTENT;
        };
    }

    public void addEdgeAndVerifyDeadlock(int fromNode, int toNode) {
        waitForGraph.addWaitEdge(fromNode, toNode);
        if (waitForGraph.hasCycle()) {
            Lock firstFromTransation = locks.stream().filter(lock -> lock.getTransactionId() == fromNode).findFirst().get();
            Lock firstToTransation = locks.stream().filter(lock -> lock.getTransactionId() == toNode).findFirst().get();

            if (firstFromTransation.getOperation().getTimestamp().after(firstToTransation.getOperation().getTimestamp())) {
                System.out.println("Abortando transação " + fromNode);
                abortTransaction(fromNode);
            } else {
                System.out.println("Abortando transação " + toNode);
                abortTransaction(toNode);
            }
        }
    }

    public void abortTransaction(int transactionId) {
        abortedTransactions.add(transactionId);
        locks.removeIf(lock -> lock.getTransactionId() == transactionId);
        scheduledOperations.removeIf(operation -> operation.getTransactionId() == transactionId);
        operations.stream()
                .filter(op -> op.getTransactionId() == transactionId)
                .forEach(op -> op.setStatus(OperationStatus.ABORTED));

        List<Integer> reachedNodes = waitForGraph.recoverReachedNodes(transactionId);

        waitForGraph.removeAllEdges(transactionId);
        Optional.ofNullable(reachedNodes)
                .ifPresent(nodes -> nodes.forEach(tId -> locks.stream()
                        .filter(lock -> lock.getTransactionId().equals(tId) && lock.getStatus().equals(LockStatus.WAITING))
                        .forEach(lock -> {
                            if (canGrantLock(lock)) {
                                scheduledOperations.add(lock.getOperation());
                                lock.setStatus(LockStatus.GRANTED);
                                lock.getOperation().setStatus(OperationStatus.EXECUTED);
                            }
                        })));
    }

    public void addCommitGrant(Operation operation) {
        Lock lock = new Lock(operation, new Row('-'));
        lock.setStatus(LockStatus.GRANTED);
        locks.add(lock);
    }

    public void addCommitWait(Operation operation) {
        Lock lock = new Lock(operation, new Row('-'));
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
                    .filter(lk -> lk.getType().equals(LockTypes.WRITE) &&
                            lk.getTransactionId().equals(transactionId))
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

        if (locks.stream().filter(lock -> lock.getTransactionId().equals(transactionId) && lock.getStatus().equals(LockStatus.WAITING)).count() > 1) {
            return false;
        }

        return locks.stream()
                .filter(lock -> lock.getType().equals(LockTypes.READ) &&
                        !lock.getTransactionId().equals(transactionId) &&
                        lock.getStatus().equals(LockStatus.GRANTED))
                .noneMatch(lock -> writeLockedObjects.contains(lock.getObject()));
    }

    public void verifyConflicyBetwenInCommonLocks(List<Lock> locksToRelease, List<Lock> otherLocks) {
        for (Lock lockToRelease: locksToRelease) {
            for (Lock otherLock: otherLocks) {
                if (lockConflict(lockToRelease, otherLock)) {
                    addEdgeAndVerifyDeadlock(lockToRelease.getTransactionId(), otherLock.getTransactionId());
                }
            }
        }
    }

    public boolean canGrantWaitingLock(Lock currentLock) {
        List<Lock> grantedLocks = locks.stream().filter(lk -> lk.getStatus().equals(LockStatus.GRANTED)).toList();

        for (Lock lock: grantedLocks) {
            if (lockConflict(currentLock, lock)) {
                addEdgeAndVerifyDeadlock(lock.getTransactionId(), currentLock.getTransactionId());
                return false;
            }
        }
        return true;
    }

    public boolean canGrantLock(Lock currentLock) {
        List<Lock> grantedLocks = locks.stream().filter(lk -> lk.getStatus().equals(LockStatus.GRANTED)).toList();
        List<Lock> sameTransactionLocks = locks
                .stream()
                .filter(lock -> lock.getTransactionId()
                        .equals(currentLock.getTransactionId()) &&
                        lock.getStatus().equals(LockStatus.WAITING) &&
                        !lock.equals(currentLock))
                .toList();

        if (!sameTransactionLocks.isEmpty() && sameTransactionLocks.get(0).getOperation().getTimestamp().before(currentLock.getOperation().getTimestamp())) {
            return false;
        }

        for (Lock lock: grantedLocks) {
            if (lockConflict(currentLock, lock)) {
                addEdgeAndVerifyDeadlock(lock.getTransactionId(), currentLock.getTransactionId());
                return false;
            }
        }
        return true;
    }

    public void removeLock(Lock lock) {
        for (Lock lk: locks) {
            if (lk.equals(lock)) {
                locks.remove(lk);
                break;
            }
        }
    }
}