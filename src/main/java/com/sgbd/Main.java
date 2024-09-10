package com.sgbd;

import com.sgbd.controllers.InputReader;
import com.sgbd.models.graphs.SerializationGraph;
import com.sgbd.models.graphs.WaitForGraph;
import com.sgbd.models.transactions.Transaction;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String entrada_ai = "r1(x)w2(y)c1r2(x)w1(x)c2";

        InputReader inputReader = new InputReader();
        List<Transaction> transactions = inputReader.readInput(entrada_ai);

        System.out.println("Transactions: ");
        for (Transaction transaction : transactions) {
            System.out.println(transaction.getId() + ": ");
            for (int i = 0; i < transaction.getOperations().size(); i++) {
                System.out.println(transaction.getOperations().get(i).getType() + " " + transaction.getOperations().get(i).getTransactionId() + " " + transaction.getOperations().get(i).getObject());
            }
        }

    }
}
