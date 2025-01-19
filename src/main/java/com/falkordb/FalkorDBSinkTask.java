package com.falkordb;

import com.falkordb.client.GraphCommands;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.dynamic.RedisCommandFactory;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FalkorDBSinkTask extends SinkTask {
    private static final Logger logger = LoggerFactory.getLogger(FalkorDBSinkTask.class);

    private RedisClient redisClient = null;
    private StatefulRedisConnection<String, String> connection = null;
    private GraphCommands customCommands = null;
    private Retry retrySpec = Retry.backoff(3, Duration.ofSeconds(20))
            .jitter(0.5);

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        logger.info("Starting FalkorDBSinkTask, properties: {}", props);
        var url = props.get(FalkorDBConnectorConfig.FALKOR_URL);
        logger.info("Connecting to FalkorDB at {}", url);
        redisClient = RedisClient.create(props.get(FalkorDBConnectorConfig.FALKOR_URL));
        connection = redisClient.connect();
        customCommands = new RedisCommandFactory(connection).getCommands(GraphCommands.class);
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            var graph = record.key();
            var cypher = record.value();
            logger.info("Processing graph: {} with cypher: {}", graph, cypher);
            List<Object> result = customCommands.graphQuery(graph.toString(), cypher.toString());
            logger.info("Query result: {}", result);
        }
    }

    @Override
    public void stop() {
        connection.close();
        redisClient.shutdown();
    }
}
