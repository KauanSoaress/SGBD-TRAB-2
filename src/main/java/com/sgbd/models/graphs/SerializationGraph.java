package com.sgbd.models.graphs;

public class SerializationGraph extends Graph {

    public SerializationGraph() {
        super();
    }

    @Override
    public void specificGraphBehavior() {
    }

    // Métodos adicionais específicos para o grafo de serialização
    public void addSerializationEdge(int fromNode, int toNode, String operation) {
        // Aqui você poderia adicionar lógica para armazenar ou usar a operação, se necessário.
        System.out.println("Adding edge due to operation: " + operation);
        addEdge(fromNode, toNode);
    }
}
