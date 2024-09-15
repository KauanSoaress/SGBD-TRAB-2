package com.sgbd.models.lockTable;

import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.lockTypes.LockTypes;
import com.sgbd.models.locks.Lock;
import com.sgbd.models.locks.LockStatus;
import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;

import java.util.ArrayList;
import java.util.List;

public class LockTable {
    private final List<Lock> locks;
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
//    private int lockConflict(Lock lockOnWait, Lock lockOnGrant) {
//        if (lockOnWait.getOperation().getObject() == lockOnGrant.getOperation().getObject()) {
//            if (lockOnWait.getType() == lockOnGrant.getType()) {
//                if (lockOnWait.getType() == LockTypes.READ) {
//                    return -1;
//                } else if (lockOnWait.getType() == LockTypes.WRITE) {
//                    return lockOnGrant.getOperation().getTransactionId();
//                } else if (lockOnWait.getType() == LockTypes.CERTIFY) {
//                    return lockOnGrant.getOperation().getTransactionId();
//                }
//            } else if (lockOnWait.getType() == LockTypes.CERTIFY || lockOnGrant.getType() == LockTypes.CERTIFY) {
//                return lockOnGrant.getOperation().getTransactionId();
//            } else if (lockOnWait.getType() == LockTypes.READ || lockOnGrant.getType() == LockTypes.READ) {
//                return -1;
//            }
//        }
//        return -1;
//    }

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

    public int grantLock(Lock lockOnWait) {
        boolean canGrant = false;
        if (locks.isEmpty()) {
            lockOnWait.setStatus(LockStatus.GRANTED);

            System.out.println("Lock granted");

            // converter
            // escalonar
        } else {
            if (lockOnWait.getOperation().getType() == OperationTypes.WRITE) {
                for (Lock l : locks) {
                    if (l.getOperation().getTransactionId() != lockOnWait.getOperation().getTransactionId() &&
                        l.getOperation().getObject() == lockOnWait.getOperation().getObject())
                    {
                        waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), l.getOperation().getTransactionId());
                        lockOnWait.setStatus(LockStatus.WAITING);
                    } else if (lockOnWait.getStatus() != LockStatus.WAITING) {
                        lockOnWait.setStatus(LockStatus.GRANTED);

                        System.out.println("Lock granted");

                        // converter
                        // escalonar
                    }
                }
            } else if (lockOnWait.getOperation().getType() == OperationTypes.READ) {
                for (Lock l : locks) {
                    if (l.getType() == LockTypes.CERTIFY &&
                        l.getOperation().getObject() == lockOnWait.getOperation().getObject())
                    {
                        waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), l.getOperation().getTransactionId());
                        lockOnWait.setStatus(LockStatus.WAITING);
                    } else if (lockOnWait.getStatus() != LockStatus.WAITING) {
                        lockOnWait.setStatus(LockStatus.GRANTED);

                        System.out.println("Lock granted");

                        // se nessa transação teve uma write converter
                        // escalonar
                    }
                }
            } else if (lockOnWait.getOperation().getType() == OperationTypes.COMMIT) {
                boolean canConvert = true;
                for (Lock l : locks) {
                    if (!canConvert) {
                        break;
                    }
                    if (l.getOperation().getTransactionId() == lockOnWait.getOperation().getTransactionId()) {
                        if (l.getStatus() == LockStatus.WAITING) {
                            canConvert = false;
                            break;
                        }
                        if (l.getType() == LockTypes.WRITE) {
                            for (Lock b : locks) {
                                if (b.getType() == LockTypes.READ &&
                                    b.getOperation().getTransactionId() != l.getOperation().getTransactionId() &&
                                    b.getOperation().getObject() == lockOnWait.getOperation().getObject())
                                {
                                    canConvert = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (canConvert) {
                    for (Lock lock : locks) {
                        if (lock.getOperation().getTransactionId() == lockOnWait.getOperation().getTransactionId()) {
                            lock.certifyLock();
                            lockOnWait.setStatus(LockStatus.GRANTED);

                            System.out.println("Lock granted");
                        }
                    }
                    // escalonar
                } else {
                    for (Lock lock : locks) {
                        if (lock.getOperation().getTransactionId() == lockOnWait.getOperation().getTransactionId()) {
                            waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), lock.getOperation().getTransactionId());
                            lockOnWait.setStatus(LockStatus.WAITING);
                        }
                    }
                }
            }
        }
        locks.add(lockOnWait);
        return waitForGraph.hasCycle() ? 1 : 0;
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

    public void convertWriteToCertify(int transactionId){
        List<Lock> writeLocks = new ArrayList<>();
        List<Lock> readLocks = new ArrayList<>();

        locks
            .forEach(
                lock -> {
                    if (lock.getTransactionId() == transactionId && lock.getType() == LockTypes.WRITE){
                        writeLocks.add(lock);
                    } else if (lock.getTransactionId() == transactionId && lock.getType() == LockTypes.READ){
                        readLocks.add(lock);
                    }
                }
            );

        if (!writeLocks.isEmpty() && !readLocks.isEmpty()){
            writeLocks
                .forEach(
                    lock -> readLocks
                        .forEach(
                            lockWithoutWrite -> {
                                waitForGraph.addWaitEdge(lock.getTransactionId(), lockWithoutWrite.getTransactionId());
                                lock.setStatus(LockStatus.WAITING);
                            }
                        )
                );
        } else {
            writeLocks
                .forEach(
                    lock -> {
                        lock.setType(LockTypes.CERTIFY);
                        lock.setStatus(LockStatus.GRANTED);
                    }
                );
        }
    }
}


