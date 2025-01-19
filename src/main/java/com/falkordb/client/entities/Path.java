package com.falkordb.client.entities;

import java.util.List;

/**
 * This class represents a path in the graph.
 */
public record Path(List<Node> nodes, List<Edge> edges) {

    /**
     * Parametrized constructor
     *
     * @param nodes - List of nodes.
     * @param edges - List of edges.
     */
    public Path {
    }

    /**
     * Returns the nodes of the path.
     *
     * @return List of nodes.
     */
    @Override
    @SuppressWarnings("unused")
    public List<Node> nodes() {
        return nodes;
    }

    /**
     * Returns the edges of the path.
     *
     * @return List of edges.
     */
    @Override
    @SuppressWarnings("unused")
    public List<Edge> edges() {
        return edges;
    }

    /**
     * Returns the length of the path - number of edges.
     *
     * @return Number of edges.
     */
    @SuppressWarnings("unused")
    public int length() {
        return edges.size();
    }

    /**
     * Return the number of nodes in the path.
     *
     * @return Number of nodes.
     */
    @SuppressWarnings("unused")
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Returns the first node in the path.
     *
     * @return First nodes in the path.
     * @throws IndexOutOfBoundsException if the path is empty.
     */
    @SuppressWarnings("unused")
    public Node firstNode() {
        return nodes.get(0);
    }

    /**
     * Returns the last node in the path.
     *
     * @return Last nodes in the path.
     * @throws IndexOutOfBoundsException if the path is empty.
     */
    @SuppressWarnings("unused")
    public Node lastNode() {
        return nodes.get(nodes.size() - 1);
    }

    /**
     * Returns a node with specified index in the path.
     *
     * @param index index of the node.
     * @return Node.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= nodesCount()})
     */
    @SuppressWarnings("unused")
    public Node getNode(int index) {
        return nodes.get(index);
    }

    /**
     * Returns an edge with specified index in the path.
     *
     * @param index index of the edge.
     * @return Edge.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= length()})
     */
    @SuppressWarnings("unused")
    public Edge getEdge(int index) {
        return edges.get(index);
    }

    @Override
    public String toString() {
        return "Path{" + "nodes=" + nodes +
                ", edges=" + edges +
                '}';
    }
}
