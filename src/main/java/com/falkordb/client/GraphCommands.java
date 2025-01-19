package com.falkordb.client;

import io.lettuce.core.dynamic.Commands;
import io.lettuce.core.dynamic.annotation.Command;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
public interface GraphCommands extends Commands {
    @Command("GRAPH.QUERY :key :value --COMPACT")
    List<Object> graphQuery(@Param("key") String graphName, @Param("value") String cypherCommand);

    @Command("GRAPH.DELETE :graphId")
    List<Object> graphDelete(@Param("graphId") String graphId);
}
