package com.falkordb;

import com.falkordb.client.Client;
import com.falkordb.client.Query;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FalkorDBSinkTask extends SinkTask {
    private static final Logger logger = LoggerFactory.getLogger(FalkorDBSinkTask.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Client client = null;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        logger.info("Starting FalkorDBSinkTask, properties: {}", props);
        var url = props.get(FalkorDBConnectorConfig.FALKOR_URL);
        client = new Client.Builder()
                .url(url)
                .build();
//            var graph = client.graph("falkordb");
//            var query = "CREATE (a:Artist {Name: 'Strapping Young Lad'}) RETURN a";
//            var rowResponse = graph.executeQuery(query);
//            logger.info("Query result: {}", rowResponse);

//        }
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            String jsonValue = (String) record.value();
            try {
                // Deserialize the JSON string into a List of Maps
                List<Query> jsonList = objectMapper.readValue(jsonValue, new TypeReference<>() {
                });
                logger.info("***** got jsonList of size***** : {}", jsonList.size());
                for (Query query : jsonList) {
                     var qr = client.execute(query);
                     logger.info("Query result: {}", qr);
                }
            } catch (IOException e) {
                logger.error("Failed to parse JSON value: {}", e.getMessage());
            }

        }
    }

    @Override
    public void stop() {
        client.close();
    }
}
