package com.falkordb.client.cache;

import com.falkordb.client.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CacheList {
    private static final Logger logger = LoggerFactory.getLogger(CacheList.class);
    private final String procedure;
    private final ConcurrentHashMap<Integer, String> data = new ConcurrentHashMap<>();

    public CacheList(String procedure) {
        this.procedure = procedure;
    }

    public String getCachedData(int index, Graph graph) {
        return data.putIfAbsent(index, getDataFromServer(index, graph));
    }

    private String getDataFromServer(int index, Graph graph) {

        if (data.containsKey(index)) {
            return data.get(index);
        }
        // the value that will add all missing values and return the requested one
        List<String> newData = getAllData(graph);
        // add all missing values
        for (int i = 0; i < newData.size(); i++) {
            data.put(i, newData.get(i));
        }
        // return the requested value
        if (newData.size() > index) {
            return newData.get(index);
        } else {
            logger.error("Index out of bounds while getting cache list of type {}, requesting index {}, values are {}", procedure, index, newData);
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
    }

    private List<String> getAllData(Graph graph) {
        return graph.executeProcedure(procedure).records().stream()
                .map(record -> record.getString(0))
                .collect(Collectors.toList());
    }

    public void clear() {
        data.clear();
    }
}