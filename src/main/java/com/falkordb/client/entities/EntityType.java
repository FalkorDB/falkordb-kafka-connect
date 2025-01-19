package com.falkordb.client.entities;

public enum EntityType {
    VALUE_UNKNOWN,
    VALUE_NULL,
    VALUE_STRING,
    VALUE_INTEGER,  // 64 bit long.
    VALUE_BOOLEAN,
    VALUE_DOUBLE,
    VALUE_ARRAY,
    VALUE_EDGE,
    VALUE_NODE,
    VALUE_PATH,
    VALUE_MAP,
    VALUE_POINT,
    VALUE_VECTORF32;

    private static final EntityType[] values = values();

    public static EntityType getValue(int index) {
        try {
            return values[index];
        } catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unrecognized response type " + index);
        }
    }

}
