package com.falkordb.client.cache;

import com.falkordb.client.Graph;

public class Cache {
    private final CacheList labels;
    private final CacheList propertyNames;
    private final CacheList relationshipTypes;

    /**
     * Default constructor
     */
    public Cache() {
        this.labels = new CacheList("db.labels");
        this.propertyNames = new CacheList("db.propertyKeys");
        this.relationshipTypes = new CacheList("db.relationshipTypes");
    }

    /**
     * @param index index of label
     * @param graph source graph
     * @return requested label
     */
    public String getLabel(int index, Graph graph) {
        return labels.getCachedData(index, graph);
    }

    /**
     * @param index index of the relationship type
     * @param graph source graph
     * @return requested relationship type
     */
    public String getRelationshipType(int index, Graph graph) {
        return relationshipTypes.getCachedData(index, graph);
    }

    /**
     * @param index index of property name
     * @param graph source graph
     * @return requested property
     */
    public String getPropertyName(int index, Graph graph) {
        return propertyNames.getCachedData(index, graph);
    }

    /**
     * Clears the cache
     */
    public void clear() {
        labels.clear();
        propertyNames.clear();
        relationshipTypes.clear();
    }
}
