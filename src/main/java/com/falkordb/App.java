package com.falkordb;

import com.falkordb.client.Driver;
import io.lettuce.core.RedisClient;
import io.lettuce.core.dynamic.RedisCommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try (var driver = new Driver.Builder()
                .url("redis://localhost:6379")
                .build()) {
            var graph = driver.graph("falkordb");
            var query = "CREATE (a:Artist {Name: 'Strapping Young Lad'}) RETURN a";
            var rowResponse = graph.executeQuery(query);
            logger.info("Query result: {}", rowResponse);
        }
    }
}

