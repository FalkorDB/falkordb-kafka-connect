package com.falkordb.client;

import com.falkordb.client.cache.Cache;
import com.falkordb.client.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class QueryResult {
    private static final Logger logger = LoggerFactory.getLogger(QueryResult.class);
    private final List<Object> rawResponse;
    private final Graph graph;
    private final Cache cache;
    private final String query;
    private final Statistics statistics;
    private final List<Record> records;
    private final Header header;

    public QueryResult(List<Object> rawResponse, Graph graph, Cache cache, String query) {
        this.rawResponse = rawResponse;
        this.graph = graph;
        this.cache = cache;
        this.query = query;
        logger.info("rawResponse: {}", rawResponse);
        if (rawResponse.size() != 3) {
            header = parseHeader(Collections.emptyList());
            records = Collections.emptyList();
            statistics = rawResponse.isEmpty() ? parseStatistics(Collections.emptyList())
                    : parseStatistics(rawResponse.get(rawResponse.size() - 1));

        } else {
            //noinspection unchecked
            header = parseHeader((List<List<Object>>) rawResponse.get(0));
            //noinspection unchecked
            records = parseRecords((List<List<Object>>) rawResponse.get(1));
            statistics = parseStatistics(rawResponse.get(2));
        }
    }

    private Header parseHeader(List<List<Object>> rawHeader) {
        return new Header(rawHeader);
    }

    private List<Record> parseRecords(List<List<Object>> rawResultSet) {
        if (rawResultSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<Record> records = new ArrayList<>(rawResultSet.size());
        for (List<Object> row : rawResultSet) {
            List<Object> parsedRow = new ArrayList<>(row.size());
            for (int i = 0; i < row.size(); i++) {
                @SuppressWarnings("unchecked") List<Object> obj = (List<Object>) row.get(i);
                // get object type
                Header.ResultSetColumnTypes objType = header.getSchemaTypes().get(i);
                // deserialize according to type and
                switch (objType) {
                    case COLUMN_NODE:
                        parsedRow.add(deserializeNode(obj));
                        break;
                    case COLUMN_RELATION:
                        parsedRow.add(deserializeEdge(obj));
                        break;
                    case COLUMN_SCALAR:
                        parsedRow.add(deserializeScalar(obj));
                        break;
                    default:
                        parsedRow.add(null);
                        break;
                }
            }
            Record record = new Record(header.getSchemaNames(), parsedRow);
            records.add(record);
        }
        return records;
    }


    public Statistics getStatistics() {
        return statistics;
    }

    public List<Record> records() {
        return records;
    }

    public Header getHeader() {
        return header;
    }

    private Statistics parseStatistics(Object rawStatistics) {
        //noinspection unchecked
        return new Statistics((List<String>) rawStatistics);
    }

    private Node deserializeNode(List<Object> rawNodeData) {

        @SuppressWarnings("unchecked") List<Long> labelsIndices = (List<Long>) rawNodeData.get(1);
        @SuppressWarnings("unchecked") List<List<Object>> rawProperties = (List<List<Object>>) rawNodeData.get(2);

        var nodeProperties = deserializeGraphEntityProperties(rawProperties);

        var nodeId = (Long) rawNodeData.get(0);

        var labels = new ArrayList<>(labelsIndices.stream()
                .map(labelIndex -> cache.getLabel(labelIndex.intValue(), graph))
                .toList());
        var node = new Node(nodeProperties, labels);
        node.setId(nodeId);


        return node;
    }

    private Map<String, Property<?>> deserializeGraphEntityProperties(List<List<Object>> rawProperties) {
        var res = new HashMap<String, Property<?>>(rawProperties.size());
        for (List<Object> rawProperty : rawProperties) {
            var propertyName = cache.getPropertyName(((Long) rawProperty.get(0)).intValue(), graph);
            // trimmed for getting to value using deserializeScalar
            List<Object> propertyScalar = rawProperty.subList(1, rawProperty.size());
            var propertyValue = deserializeScalar(propertyScalar);
            Property<?> property = new Property<>(propertyName, propertyValue);
            res.put(property.name(), property);
        }
        return res;
    }

    @Nullable
    private Object deserializeScalar(List<Object> rawScalarData) {
        EntityType type = getValueTypeFromObject(rawScalarData.get(0));

        Object obj = rawScalarData.get(1);
        switch (type) {
            case VALUE_NULL -> {
                return null;
            }
            case VALUE_BOOLEAN -> {
                return Boolean.parseBoolean(Util.encode(obj));
            }
            case VALUE_DOUBLE -> {
                return Double.parseDouble(Util.encode(obj));
            }
            case VALUE_STRING -> {
                return Util.encode(obj);
            }
            case VALUE_ARRAY -> {
                return deserializeArray(obj);
            }
            case VALUE_NODE -> {
                //noinspection unchecked
                return deserializeNode((List<Object>) obj);
            }
            case VALUE_EDGE -> {
                //noinspection unchecked
                return deserializeEdge((List<Object>) obj);
            }
            case VALUE_PATH -> {
                return deserializePath(obj);
            }
            case VALUE_MAP -> {
                return deserializeMap(obj);
            }
            case VALUE_POINT -> {
                return deserializePoint(obj);
            }
            case VALUE_VECTORF32 -> {
                return deserializeVector(obj);
            }
            default -> {
                return obj;
            }
        }
    }

    private List<Float> deserializeVector(Object rawScalarData) {
        //noinspection unchecked
        List<Double> doubles = (List<Double>) rawScalarData;
        return doubles.stream().map(Double::floatValue).collect(Collectors.toList());
    }


    private Point deserializePoint(Object rawScalarData) {
        return new Point(Util.toListOfDouble(rawScalarData));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeMap(Object rawScalarData) {
        List<Object> keyTypeValueEntries = (List<Object>) rawScalarData;

        int size = keyTypeValueEntries.size();
        // set the capacity to half of the list size
        Map<String, Object> map = new HashMap<>(size >> 1);

        for (int i = 0; i < size; i += 2) {
            String key = (String) keyTypeValueEntries.get(i);
            Object value = deserializeScalar((List<Object>) keyTypeValueEntries.get(i + 1));
            map.put(key, value);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Path deserializePath(Object rawScalarData) {
        List<List<Object>> array = (List<List<Object>>) rawScalarData;
        List<Node> nodes = (List<Node>) deserializeScalar(array.get(0));
        List<Edge> edges = (List<Edge>) deserializeScalar(array.get(1));
        return new Path(nodes, edges);
    }

    private Edge deserializeEdge(List<Object> rawEdgeData) {

        //noinspection unchecked
        List<List<Object>> rawProperties = (List<List<Object>>) rawEdgeData.get(4);

        var edgeId = (Long) rawEdgeData.get(0);

        String relationshipType = cache.getRelationshipType(((Long) rawEdgeData.get(1)).intValue(), graph);

        var source = (long) rawEdgeData.get(2);
        var destination = (long) rawEdgeData.get(3);

        var properties = deserializeGraphEntityProperties(rawProperties);
        var edge = new Edge(properties, relationshipType, source, destination);
        edge.setId(edgeId);
        return edge;
    }

    private List<Object> deserializeArray(Object rawScalarData) {
        @SuppressWarnings("unchecked") List<List<Object>> array = (List<List<Object>>) rawScalarData;
        List<Object> res = new ArrayList<>(array.size());
        for (List<Object> arrayValue : array) {
            res.add(deserializeScalar(arrayValue));
        }
        return res;
    }

    private EntityType getValueTypeFromObject(Object rawScalarType) {
        return EntityType.getValue(((Long) rawScalarType).intValue());
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "graph=" + graph +
                ", query='" + query + '\'' +
                ", rawResponse='" + rawResponse + '\'' +
                '}';
    }


}
