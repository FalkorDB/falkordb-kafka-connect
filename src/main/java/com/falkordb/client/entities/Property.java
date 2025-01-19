package com.falkordb.client.entities;

import java.util.Objects;

public record Property<T>(String name, T value) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property<?> property)) return false;
        return Objects.equals(name, property.name) &&
                valueEquals(value, property.value);
    }

    private boolean valueEquals(Object value1, Object value2) {
        if (value1 instanceof Integer) value1 = ((Integer) value1).longValue();
        if (value2 instanceof Integer) value2 = ((Integer) value2).longValue();
        return Objects.equals(value1, value2);
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
