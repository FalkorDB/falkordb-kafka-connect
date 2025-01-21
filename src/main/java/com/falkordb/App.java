package com.falkordb;

import com.falkordb.client.Client;
import com.falkordb.client.GraphCommand;
import com.falkordb.client.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Hello world!
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {

        try (var client = new Client.Builder()
                .url("redis://localhost:6379")
                .build()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("name_param", "Person");
            parameters.put("age_param", 20);
            parameters.put("location_param", "Location 1");

            // Create a query command (you may have specific commands in your GraphCommand enum)
            GraphCommand command = GraphCommand.GRAPH_QUERY; // Assuming a GRAPH_QUERY command exists

            // Build the Cypher command
            String cypherCommand = "CREATE (p:Person {name: $name_param, age: $age_param, location: $location_param}) RETURN p";

            // Use the builder to create the Query object
            var q = new Query.Builder()
                    .graphName("falkordb")
                    .command(command)
                    .cypherCommand(cypherCommand)
                    .parameters(parameters)
                    .build();
            var r = client.execute(q);
            logger.info("Query result: {}", r);
            r = client.execute(q);
            logger.info("Query result: {}", r);
        }
//        writeQueriesToJsonFile(generateQueries(1000), "queries.json");
    }

    public static List<Query> generateQueries(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    // Create a map of parameters for each query
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("name_param", "Person " + i);
                    parameters.put("age_param", 20 + i);
                    parameters.put("location_param", "Location " + (i % 5)); // Cycle through 5 locations

                    // Create a query command (you may have specific commands in your GraphCommand enum)
                    GraphCommand command = GraphCommand.GRAPH_QUERY; // Assuming a GRAPH_QUERY command exists

                    // Build the Cypher command
                    String cypherCommand = "CREATE (p:Person {name: $name_param, age: $age_param, location: $location_param}) RETURN p";

                    // Use the builder to create the Query object
                    return new Query.Builder()
                            .graphName("falkordb")
                            .command(command)
                            .cypherCommand(cypherCommand)
                            .parameters(parameters)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static void writeQueriesToJsonFile(List<Query> queries, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            int batchSize = 10; // Define the batch size
            int totalQueries = queries.size();

            for (int i = 0; i < totalQueries; i += batchSize) {
                // Calculate the end index for the current batch
                int end = Math.min(i + batchSize, totalQueries);

                // Create a sublist for the current batch
                List<Query> batch = queries.subList(i, end);

                // Convert the batch to JSON array format
                String jsonBatch = objectMapper.writeValueAsString(batch);

                // Write the JSON array to the file
                writer.write(jsonBatch);
                writer.newLine(); // Write each batch on a new line

                logger.info("Successfully wrote batch {} of {} to {}", (i / batchSize) + 1, (totalQueries + batchSize - 1) / batchSize, filePath);
            }

            logger.info("Successfully wrote all {} queries to {}", totalQueries, filePath);
        } catch (IOException e) {
            logger.error("Error writing queries to file: {}", e.getMessage());
        }
    }

}


