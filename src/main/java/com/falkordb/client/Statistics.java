package com.falkordb.client;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Statistics {

    public enum Label {
        LABELS_ADDED("Labels added"),
        INDICES_ADDED("Indices created"),
        INDICES_DELETED("Indices deleted"),
        NODES_CREATED("Nodes created"),
        NODES_DELETED("Nodes deleted"),
        RELATIONSHIPS_DELETED("Relationships deleted"),
        PROPERTIES_SET("Properties set"),
        RELATIONSHIPS_CREATED("Relationships created"),
        CACHED_EXECUTION("Cached execution"),
        QUERY_INTERNAL_EXECUTION_TIME("Query internal execution time");

        private final String text;

        Label(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }

        /**
         * Get a Label by label text
         *
         * @param value label text
         * @return the matching Label
         */
        public static Optional<Label> getEnum(String value) {
            for (Label v : values()) {
                if (v.toString().equalsIgnoreCase(value)) return Optional.of(v);
            }
            return Optional.empty();
        }
    }

    private final List<String> raw;
    private final Map<Label, String> statistics;

    public Statistics(List<String> raw) {
        this.raw = raw;
        this.statistics = new EnumMap<>(Statistics.Label.class);
    }


    private Map<Statistics.Label, String> getStatistics() {
        if (statistics.isEmpty()) {
            for (String text : this.raw) {
                String[] rowTuple = text.split(":");
                if (rowTuple.length == 2) {
                    Optional<Statistics.Label> optionalLabel = Statistics.Label.getEnum(rowTuple[0]);
                    if (optionalLabel.isPresent()) {
                        Statistics.Label label = optionalLabel.get();
                        this.statistics.put(label, rowTuple[1].trim());
                    }
                }
            }
        }
        return statistics;
    }

    public Optional<String> getStringValue(Statistics.Label label) {
        return Optional.ofNullable(getStatistics().get(label));
    }

    public int getIntValue(Statistics.Label label) {
        return getStringValue(label).map(Integer::parseInt).orElse(0);
    }

    public int nodesCreated() {
        return getIntValue(Label.NODES_CREATED);
    }

    public int nodesDeleted() {
        return getIntValue(Label.NODES_DELETED);
    }

    int indicesAdded() {
        return getIntValue(Label.INDICES_ADDED);
    }

    int indicesDeleted() {
        return getIntValue(Label.INDICES_DELETED);
    }

    int labelsAdded() {
        return getIntValue(Label.LABELS_ADDED);
    }

    int relationshipsDeleted() {
        return getIntValue(Label.RELATIONSHIPS_DELETED);
    }

    int relationshipsCreated() {
        return getIntValue(Label.RELATIONSHIPS_CREATED);
    }

    public int propertiesSet() {
        return getIntValue(Label.PROPERTIES_SET);
    }

    boolean cachedExecution() {
        return getStringValue(Label.CACHED_EXECUTION)
                .map(value -> !value.equals("0"))
                .orElse(false);
    }

    public Optional<String> getInternalExecutionTime() {
        return getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME);
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "raw=" + raw +
                ", statistics=" + statistics +
                '}';
    }
}
