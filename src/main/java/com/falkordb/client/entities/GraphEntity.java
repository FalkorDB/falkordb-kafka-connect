package com.falkordb.client.entities;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class GraphEntity {
    protected long id;
    protected final Map<String, Property<?>> propertyMap;


    public GraphEntity(Map<String, Property<?>> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public GraphEntity(int propertiesCapacity) {
        propertyMap = new HashMap<>(propertiesCapacity);
    }

    /**
     * @param id - entity id to be set
     */
    public void setId(long id) {
        this.id = id;
    }


    /**
     * @return Entity's property names, as a Set
     */
    public Set<String> getEntityPropertyNames() {
        return propertyMap.keySet();
    }

    /**
     * Add a property to the entity
     *
     * @param property property object
     */
    public void addProperty(Property<?> property) {
        propertyMap.put(property.name(), property);
    }

    /**
     * @return number of properties
     */
    public int getNumberOfProperties() {
        return propertyMap.size();
    }


    /**
     * @param propertyName - property name as lookup key (String)
     * @return property object, or null if key is not found
     */
    public Property<?> getProperty(String propertyName) {
        return propertyMap.get(propertyName);
    }


    /**
     * @param name - the name of the property to be removed
     */
    public void removeProperty(String name) {
        propertyMap.remove(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphEntity that)) {
            return false;
        }
        return id == that.id &&
                Objects.equals(propertyMap, that.propertyMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, propertyMap);
    }

    @Override
    public abstract String toString();
}

