package com.falkordb;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

public class FalkorDBConnectorConfig {

    public static final String FALKOR_URL = "falkor.url";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(FALKOR_URL,
                    Type.STRING,
                    "redis://localhost:6379",
                    new ConfigDef.NonEmptyString(),
                    Importance.HIGH,
                    "redis :// [password@] host [: port] [/ database]\n" +
                            "  [? [timeout=timeout[d|h|m|s|ms|us|ns]]\n" +
                            "  [&_database=database_]]\n" +
                            "  There are four URI schemes:\n" +
                            "\n" +
                            "redis – a standalone Redis server\n" +
                            "rediss – a standalone Redis server via an SSL connection\n" +
                            "redis-socket – a standalone Redis server via a Unix domain socket\n" +
                            "redis-sentinel – a Redis Sentinel server\n");
}
