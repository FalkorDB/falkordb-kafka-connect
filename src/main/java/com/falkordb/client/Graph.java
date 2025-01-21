package com.falkordb.client;

import com.falkordb.client.cache.Cache;
import io.lettuce.core.RedisCommandExecutionException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Graph {
    private final GraphCommands graphCommands;
    private final String graphId;
    private final Cache cache;

    Graph(GraphCommands graphCommands, String graphId) {
        this.graphCommands = graphCommands;
        this.graphId = graphId;
        this.cache = new Cache();
    }


    public QueryResult executeQuery(String query) {
        List<Object> rowResponse = graphCommands.graphQuery(this.graphId, query);
        return new QueryResult(rowResponse, this, query);
    }

    public QueryResult executeQuery(String query, Map<String, Object> params) {
        String preparedQuery = Util.prepareQuery(query, params);
        return executeQuery(preparedQuery);
    }

    public QueryResult executeProcedure(String procedure, List<String> args, Map<String, List<String>> kwargs) {
        var prepareProcedure = Util.prepareProcedure(procedure, args, kwargs);
        List<Object> rowResponse = graphCommands.graphQuery(this.graphId, prepareProcedure);
        return new QueryResult(rowResponse, this, prepareProcedure);
    }

    public QueryResult executeProcedure(String procedure) {
        return executeProcedure(procedure, Collections.emptyList(), Collections.emptyMap());
    }

    @SuppressWarnings("unused")
    public QueryResult executeProcedure(String procedure, List<String> args) {
        return executeProcedure(procedure, args, Collections.emptyMap());
    }

    @SuppressWarnings("UnusedReturnValue")
    public String delete() {
        try {
            cache.clear();
            Object rowResponse = graphCommands.graphDelete(this.graphId);
            return rowResponse.toString();
        } catch (RedisCommandExecutionException e) {
            if ("ERR Invalid graph operation on empty key".equals(e.getMessage())) {
                return "NOT FOUND";
            } else {
                throw e;
            }
        }


//        return new QueryResult(rowResponse, this, this.cache, String.format("delete graph %s", graphId));
    }

    Cache cache() {
        return cache;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "graphId='" + graphId + '\'' +
                '}';
    }

}
