package com.falkordb.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testBuilderCreatesQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("name_param", "Niccolò Machiavelli");

        Query query = new Query.Builder()
                .graphName("myGraph")
                .command(GraphCommand.GRAPH_QUERY)
                .cypherCommand("MATCH (p:Person {name: $name_param}) RETURN p")
                .parameters(params)
                .build();

        assertEquals("myGraph", query.graphName());
        assertEquals(GraphCommand.GRAPH_QUERY, query.command());
        assertEquals("MATCH (p:Person {name: $name_param}) RETURN p", query.cypherCommand());
        assertEquals(params, query.parameters());
    }

    @Test
    void testJsonSerialization() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("name_param", "Niccolò Machiavelli");

        Query query = new Query("myGraph", GraphCommand.GRAPH_QUERY,
                "MATCH (p:Person {name: $name_param}) RETURN p", params);

        String json = objectMapper.writeValueAsString(query);
        assertTrue(json.contains("\"graphName\":\"myGraph\""));
        assertTrue(json.contains("\"command\":\"GRAPH_QUERY\""));
        assertTrue(json.contains("\"cypherCommand\":\"MATCH (p:Person {name: $name_param}) RETURN p\""));
        assertTrue(json.contains("\"parameters\":{\"name_param\":\"Niccolò Machiavelli\"}"));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"graphName\":\"myGraph\",\"command\":\"GRAPH_QUERY\",\"cypherCommand\":\"MATCH (p:Person {name: $name_param}) RETURN p\",\"parameters\":{\"name_param\":\"Niccolò Machiavelli\"}}";

        Query query = objectMapper.readValue(json, Query.class);

        assertEquals("myGraph", query.graphName());
        assertEquals(GraphCommand.GRAPH_QUERY, query.command());
        assertEquals("MATCH (p:Person {name: $name_param}) RETURN p", query.cypherCommand());
        assertEquals("Niccolò Machiavelli", query.parameters().get("name_param"));
    }

    @Test
    void testParametersInitializationToEmptyMap() {
        Query query = new Query("myGraph", GraphCommand.GRAPH_QUERY,
                "MATCH (p:Person) RETURN p", null);

        assertNotNull(query.parameters());
        assertTrue(query.parameters().isEmpty());
    }

    @Test
    void testToCacheableString() {
        Map<String, Object> params = new HashMap<>();
        params.put("name_param", "Niccolò Machiavelli");

        Query query = new Query("myGraph", GraphCommand.GRAPH_QUERY,
                "MATCH (p:Person {name: $name_param}) RETURN p", params);

        String expectedCacheableString = "GRAPH_QUERY name_param = 'Niccolò Machiavelli' MATCH (p:Person {name: $name_param}) RETURN p";

        assertEquals(expectedCacheableString, query.toCacheableString());
    }

}
