package com.falkordb.client.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Node extends GraphEntity {
    private final List<String> labels;

    public Node(Map<String, Property<?>> propertyMap) {
        super(propertyMap);
        labels = new ArrayList<>();
    }

    public Node(Map<String, Property<?>> propertyMap, List<String> labels) {
        super(propertyMap);
        this.labels = new ArrayList<>(labels);
    }

    public Node(long id, Map<String, Property<?>> propertyMap, List<String> labels) {
        super(propertyMap);
        this.labels = new ArrayList<>(labels);
        this.id = id;
    }

    public void addLabel(String label) {
        labels.add(label);
    }

    public void removeLabel(String label) {
        labels.remove(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(labels, node.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), labels);
    }

    @Override
    public String toString() {
        return "Node{" +
                "labels=" + labels +
                ", id=" + id +
                ", propertyMap=" + propertyMap +
                '}';
    }
}
