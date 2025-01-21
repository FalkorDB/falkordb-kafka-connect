package com.falkordb.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.dynamic.RedisCommandFactory;

import java.time.Duration;
import java.util.Collections;

public class Client implements AutoCloseable{
    private final RedisClient redisClient;
    private final StatefulRedisConnection<?, ?> connection;
    private final GraphCommands graphCommands;
    private final Cache<String, Graph> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    // Private constructor
    private Client(Builder builder) {
        String url = builder.url;
        redisClient = RedisClient.create(url);
        connection = redisClient.connect();
        graphCommands = new RedisCommandFactory(connection).getCommands(GraphCommands.class);

    }


    public Graph graph(String graphId) {
        return new Graph(this.graphCommands, graphId);
    }

    public QueryResult execute(Query query) {
        Graph graph = cache.get(query.graphName(), graphId -> new Graph(this.graphCommands, graphId));
        assert graph != null;
        var cypherCommand = query.toCacheableString();
        return switch (query.command()) {
            case GRAPH_QUERY -> graph.executeQuery(cypherCommand);
            case GRAPH_DELETE -> {
                graph.delete();
                yield new QueryResult(Collections.emptyList(), graph, "delete graph " + query.graphName());
            }
        };
    }

    @Override
    public void close()  {
        connection.close();
        redisClient.shutdown();
    }

    public static class Builder {
        private  String url;

        // Builder constructor with required fields
        public Builder() {
            url = "redis://localhost:6379";
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        // Build method to create Client object
        public Client build() {
            return new Client(this);
        }
    }
}

