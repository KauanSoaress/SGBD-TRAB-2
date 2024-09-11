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
                } else if (lockOnWait.getType() == LockTypes.CERTIFY) {
                    return lockOnGrant.getOperation().getTransactionId();
                }
            } else if (lockOnWait.getType() == LockTypes.CERTIFY || lockOnGrant.getType() == LockTypes.CERTIFY) {
                return lockOnGrant.getOperation().getTransactionId();
            } else if (lockOnWait.getType() == LockTypes.READ || lockOnGrant.getType() == LockTypes.READ) {
                return -1;
            }
        }
        return -1;
    }

    public int grantLock(Lock lockOnWait) {
//        int conflictingTransaction = -1;
//        for (Lock l: locks){
//            conflictingTransaction = lockConflict(lockOnWait, l);
//            if (conflictingTransaction != -1){
//                waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), conflictingTransaction);
//                lockOnWait.setStatus(LockStatus.WAITING);
//            }
//            if (waitForGraph.hasCycle()){
//                return 1;
//            }
//        }
            if (lockOnWait.getOperation().getType() == OperationTypes.WRITE) {
                for (Lock l: locks) {
                    if (l.getOperation().getTransactionId() != lockOnWait.getOperation().getTransactionId() && l.getOperation().getObject() == lockOnWait.getOperation().getObject()) {
                        waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), l.getOperation().getTransactionId());
                        lockOnWait.setStatus(LockStatus.WAITING);
                    } else if (lockOnWait.getStatus() != LockStatus.WAITING){
                        lockOnWait.setStatus(LockStatus.GRANTED);
                        locks.add(lockOnWait);
                        // converter
                        // escalonar
                    }
                }
            }
            if (lockOnWait.getOperation().getType() == OperationTypes.READ) {
                for (Lock l: locks) {
                    if (l.getType() == LockTypes.CERTIFY && l.getOperation().getObject() == lockOnWait.getOperation().getObject()) {
                        waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), l.getOperation().getTransactionId());
                        lockOnWait.setStatus(LockStatus.WAITING);
                    } else if (lockOnWait.getStatus() != LockStatus.WAITING) {
                        lockOnWait.setStatus(LockStatus.GRANTED);
                        locks.add(lockOnWait);
                        // converter
                        // escalonar
                    }
                }
            }
            if (lockOnWait.getOperation().getType() == OperationTypes.COMMIT) {
                // converter todos os wl em cl
                boolean canConvert = true;
                for (Lock l: locks) {
                    if (l.getType() == LockTypes.WRITE && l.getOperation().getTransactionId() == lockOnWait.getOperation().getTransactionId()) {
                        for (Lock b : locks) {
                            if (b.getType() == LockTypes.READ &&
                                    b.getOperation().getTransactionId() != l.getOperation().getTransactionId() &&
                                    b.getOperation().getObject() == lockOnWait.getOperation().getObject()){
                                canConvert = false;
                            }
                        }
                        if (canConvert) {
                            l.certifyLock();
                            // tem que ver se o status não é waiting
                        } else {
                            waitForGraph.addWaitEdge(lockOnWait.getOperation().getTransactionId(), l.getOperation().getTransactionId());
                            lockOnWait.setStatus(LockStatus.WAITING);
                        }
                    }
                }
                // se tiver rl no objeto alvo de outra transação aguarda
                // senão concede cl
                // escalonar
            }

        lockOnWait.setStatus(LockStatus.GRANTED);
        locks.add(lockOnWait);
        System.out.println("Lock granted");
        return 0;
    }
}


