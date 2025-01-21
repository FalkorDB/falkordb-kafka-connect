package com.falkordb.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;


public record Query(String graphName, GraphCommand command, String cypherCommand, Map<String,?> parameters) {

    public static class Builder {
        @Nullable
        private String graphName;
        @Nullable
        private GraphCommand command;
        @Nullable
        private String cypherCommand;
        private Map<String, ?> parameters = new HashMap<>();

        public Builder graphName(String graphName) {
            this.graphName = graphName;
            return this;
        }

        public Builder command(GraphCommand command) {
            this.command = command;
            return this;
        }

        public Builder cypherCommand(String cypherCommand) {
            this.cypherCommand = cypherCommand;
            return this;
        }

        public Builder parameters(Map<String, ?> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Query build() {
            // need to assert that all fields are not null here
            assert graphName != null;
            assert command != null;
            assert cypherCommand != null;
            return new Query(graphName, command, cypherCommand, parameters);
        }
    }


    @JsonCreator
    public Query(
            @JsonProperty("graphName") String graphName,
            @JsonProperty("command") GraphCommand command,
            @JsonProperty("cypherCommand") String cypherCommand,
            @Nullable @JsonProperty("parameters") Map<String, ?> parameters) {
        this.graphName = graphName;
        this.command = command;
        this.cypherCommand = cypherCommand;
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    /**
     * Returns a Cypher query string representation of this query.
     * If params are present, they will be encoded in cypher standard format, for example:
     * CYPHER name_param = 'Niccolò Machiavelli' birth_year_param = 1469 MATCH (p:Person {name: $name_param, birth_year: $birth_year_param}) RETURN p
     */
    public String toCacheableString() {
        StringBuilder queryBuilder = new StringBuilder();


        // Append parameters if present
        if (!parameters.isEmpty()) {
            queryBuilder.append("CYPHER ");
            parameters.forEach((key, value) -> {
                // Format each parameter as "key = value"
                String formattedValue = formatValue(value);
                queryBuilder.append(key).append(" = ").append(formattedValue).append(" ");
            });
        }
        queryBuilder.append(cypherCommand);

        return queryBuilder.toString().trim(); // Return the final query string
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            // Escape single quotes for strings and wrap in single quotes
            return "'" + ((String) value).replace("'", "''") + "'";
        } else if (value instanceof Number || value instanceof Boolean) {
            // Directly return numbers and booleans
            return value.toString();
        } else {
            // For other types, you might want to handle them differently or throw an exception
            throw new IllegalArgumentException("Unsupported parameter type: " + value.getClass());
        }
    }
}

