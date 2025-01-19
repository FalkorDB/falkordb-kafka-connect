package com.falkordb.client;

public enum GraphCommand {
    GRAPH_QUERY("GRAPH.QUERY"),
    GRAPH_DELETE("GRAPH.DELETE");

    private final String command;

    GraphCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
