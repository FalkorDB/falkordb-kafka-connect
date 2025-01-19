package com.falkordb.client;

import java.util.List;
import java.util.Objects;

public class Record {
    private final List<String> header;
    private final List<Object> values;

    public Record(List<String> header, List<Object> values) {
        this.header = header;
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(int index) {
        return (T) this.values.get(index);
    }

    public <T> T getValue(String key) {
        return getValue(this.header.indexOf(key));
    }

    public String getString(int index) {
        return this.values.get(index).toString();
    }

    public String getString(String key) {
        return getString(this.header.indexOf(key));
    }

    public List<String> keys() {
        return header;
    }

    public List<Object> values() {
        return this.values;
    }

    public int size() {
        return this.header.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Record record)) return false;
        return Objects.equals(header, record.header) &&
                Objects.equals(values, record.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, values);
    }

    @Override
    public String toString() {
        return "Record{" + "values=" + values + '}';
    }
}