package com.falkordb.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Header {
    public enum ResultSetColumnTypes {
        COLUMN_UNKNOWN,
        COLUMN_SCALAR,
        COLUMN_NODE,
        COLUMN_RELATION
    }

    private final List<List<Object>> raw;
    private final List<String> schemaNames = new ArrayList<>();
    private final List<ResultSetColumnTypes> schemaTypes = new ArrayList<>();

    public Header(List<List<Object>> raw) {
        this.raw = raw;
    }
    public List<String> getSchemaNames() {
        if (schemaNames.isEmpty()) {
            buildSchema();
        }
        return schemaNames;
    }

    public List<ResultSetColumnTypes> getSchemaTypes() {
        if (schemaTypes.isEmpty()) {
            buildSchema();
        }
        return schemaTypes;
    }

    private void buildSchema() {
        for (List<Object> tuple : this.raw) {

            //get type
            ResultSetColumnTypes type = ResultSetColumnTypes.values()[((Long) tuple.get(0)).intValue()];
            //get text
            String text = String.class.equals(tuple.get(1).getClass()) ? (String)tuple.get(1) : Util.encode(tuple.get(1));
            if (type != null) {
                schemaTypes.add(type);
                schemaNames.add(text);
            }
        }
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Header header)) return false;
        return Objects.equals(getSchemaTypes(), header.getSchemaTypes()) &&
                Objects.equals(getSchemaNames(), header.getSchemaNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSchemaTypes(), getSchemaNames());
    }

    @Override
    public String toString() {
        return "Header{" + "schemaTypes=" + schemaTypes +
                ", schemaNames=" + schemaNames +
                '}';
    }


}
