package com.sgbd.controllers;

import com.sgbd.models.operationTypes.OperationTypes;
import com.sgbd.models.operations.Operation;
import com.sgbd.models.transactions.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputReader {

    private static List<String> parseOperationsString (String input) {
        List<String> operationsString = new ArrayList<>();
        StringBuilder currentOperationString = new StringBuilder();
        boolean isCertifyLock = false;

        for (char c : input.toCharArray()) {
            currentOperationString.append(c);
            if (isCertifyLock) {
                operationsString.add(currentOperationString.toString());
                currentOperationString.setLength(0);
                isCertifyLock = false;
            }
            else if (c == 'c'){
                isCertifyLock = true;
            }
            else if (c == ')') {
                operationsString.add(currentOperationString.toString());
                currentOperationString.setLength(0);
            }
        }

        return operationsString;
    }


    public static List<Operation> readInput(String input) {
        List<String> operationsString = parseOperationsString(input);
        List<Operation> operationsList = new ArrayList<>();
        Operation currentOperation = null;

        // operation string = r4(x), w2(y), c4, etc
        for (String operation : operationsString) {
            if (operation.charAt(0) == 'r') {
                currentOperation = new Operation(OperationTypes.READ, Integer.parseInt(operation.substring(1, 2)), operation.charAt(3));
            } else if (operation.charAt(0) == 'w') {
                currentOperation = new Operation(OperationTypes.WRITE, Integer.parseInt(operation.substring(1, 2)), operation.charAt(3));
            } else if (operation.charAt(0) == 'c') {
                currentOperation = new Operation(OperationTypes.COMMIT, Integer.parseInt(operation.substring(1, 2)));
            } else if (operation.charAt(0) == 'u') {
                currentOperation = new Operation(OperationTypes.UPDATE, Integer.parseInt(operation.substring(1, 2)), operation.charAt(3));
            }
            operationsList.add(currentOperation);
            currentOperation = null;
        }
        return operationsList;
    }

//    public List<Transaction> readInput(String input) {
//        List<Operation> operations = parseOperations(input);
//        List<Transaction> transactions = new ArrayList<>();
//        Set<Integer> transactionIds = new HashSet<>();
//        Transaction newTransaction = null;
//
//        for (Operation operation : operations) {
//            if (!transactionIds.contains(operation.getTransactionId())) {
//                newTransaction = new Transaction(operation.getTransactionId());
//                newTransaction.addOperation(operation);
//                transactions.add(newTransaction);
//                transactionIds.add(operation.getTransactionId());
//                newTransaction = null;
//            }
//            else {
//                for (Transaction transaction : transactions) {
//                    if (transaction.getId() == operation.getTransactionId()) {
//                        transaction.addOperation(operation);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return transactions;
//
//    }
}