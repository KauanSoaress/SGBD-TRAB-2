package com.sgbd.models.graphs;

public class WaitForGraph extends Graph {

    public WaitForGraph() {
        super();
    }

    @Override
    public void specificGraphBehavior() {
    }

    // Métodos adicionais específicos para o grafo de espera
    public void addWaitEdge(int fromNode, int toNode) {
        System.out.println("Adding wait-for edge between: " + fromNode + " and " + toNode);
        addEdge(fromNode, toNode);
    }
}
