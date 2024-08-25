package com.sgbd.graphs;

public class WaitForGraph extends Graph {

    public WaitForGraph() {
        super();
    }

    @Override
    public void specificGraphBehavior() {
    }

    // Métodos adicionais específicos para o grafo de espera
    public void addWaitEdge(int fromNode, int toNode) {
        // Implementar lógica para adicionar uma aresta de espera
        System.out.println("Adding wait-for edge between: " + fromNode + " and " + toNode);
        addEdge(fromNode, toNode);
    }
}
