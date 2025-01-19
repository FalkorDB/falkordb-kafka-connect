package com.falkordb.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.dynamic.RedisCommandFactory;

public class Driver implements AutoCloseable{
    private final RedisClient redisClient;
    private final StatefulRedisConnection<?, ?> connection;
    private final GraphCommands graphCommands;

    // Private constructor
    private Driver(Builder builder) {
        String url = builder.url;
        redisClient = RedisClient.create(url);
        connection = redisClient.connect();
        graphCommands = new RedisCommandFactory(connection).getCommands(GraphCommands.class);
    }


    public Graph graph(String graphId) {
        return new Graph(this.graphCommands, graphId);
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

        // Build method to create Driver object
        public Driver build() {
            return new Driver(this);
        }
    }
}

