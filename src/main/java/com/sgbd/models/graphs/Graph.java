package com.sgbd.models.graphs;

import java.util.*;

abstract class Graph {
    protected Map<Integer, Set<Integer>> adjacencyList;

    protected Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addNode(int node) {
        adjacencyList.putIfAbsent(node, new HashSet<>());
    }

    public void addEdge(int fromNode, int toNode) {
        addNode(fromNode);
        addNode(toNode);
        adjacencyList.get(fromNode).add(toNode);
    }

    public void removeAllEdges(int fromNode) {
        adjacencyList.remove(fromNode);

        for (Integer node : adjacencyList.keySet()) {
            adjacencyList.get(node).remove(fromNode);
        }
    }

    public boolean hasCycle() {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> recStack = new HashSet<>();

        for (Integer node : adjacencyList.keySet()) {
            if (hasCycleUtil(node, visited, recStack)) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> recoverReachedNodes(int node) {
        return new ArrayList<>(adjacencyList.get(node));
    }

    private boolean hasCycleUtil(int node, Set<Integer> visited, Set<Integer> recStack) {
        if (recStack.contains(node)) {
            return true;
        }

        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recStack.add(node);

        for (Integer neighbor : adjacencyList.get(node)) {
            if (hasCycleUtil(neighbor, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(node);
        return false;
    }

    public abstract void specificGraphBehavior();
}
