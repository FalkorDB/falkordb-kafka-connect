package com.falkordb.client.entities;

import java.util.Map;
import java.util.Objects;

public class Edge extends GraphEntity{
    private final String relationshipType;
    private final long source;
    private final long destination;

    public Edge(Map<String, Property<?>> propertyMap, String relationshipType, long source, long destination) {
        super(propertyMap);
        this.relationshipType = relationshipType;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Edge edge)) return false;
        if (!super.equals(o)) return false;
        return source == edge.source && destination == edge.destination && Objects.equals(relationshipType, edge.relationshipType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), relationshipType, source, destination);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "relationshipType='" + relationshipType + '\'' +
                ", source=" + source +
                ", destination=" + destination +
                ", id=" + id +
                ", propertyMap=" + propertyMap +
                '}';
    }
}
