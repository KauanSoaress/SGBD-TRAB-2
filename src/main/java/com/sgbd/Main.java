package com.sgbd;

import com.sgbd.controllers.InputReader;
import com.sgbd.controllers.Scheduler;
import com.sgbd.models.operations.Operation;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String entrada_ai = "r1(x)r2(x)w1(x)r1(y)c1w2(u)r3(u)c2w3(y)c3";

        List<Operation> operations = InputReader.readInput(entrada_ai);
        Scheduler scheduler = new Scheduler(operations);
        System.out.println(scheduler.schedule(operations));
        // PRINT DO SCHEDULE
        for (Operation operation: scheduler.getScheduledOperations()) {
            System.out.println(operation.getTransactionId() + " " + operation.getType() + " " + operation.getObject());
        }
    }
}
