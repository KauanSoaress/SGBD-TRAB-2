package com.sgbd;

import com.sgbd.graphs.SerializationGraph;
import com.sgbd.graphs.WaitForGraph;

public class Main {
    public static void main(String[] args) {
        SerializationGraph sGraph = new SerializationGraph();
        sGraph.addSerializationEdge(1, 2, "READ");
        sGraph.addSerializationEdge(2, 3, "WRITE");
        System.out.println("Has cycle in Serialization Graph: " + sGraph.hasCycle());

        WaitForGraph wGraph = new WaitForGraph();
        wGraph.addWaitEdge(1, 2);
        wGraph.addWaitEdge(2, 3);
        wGraph.addWaitEdge(3, 1); // Cria um ciclo
        System.out.println("Has cycle in Wait-for Graph: " + wGraph.hasCycle());
    }
}
