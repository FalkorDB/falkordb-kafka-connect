package com.falkordb.client;

import com.falkordb.client.entities.Edge;
import com.falkordb.client.entities.Node;
import com.falkordb.client.entities.Point;
import com.falkordb.client.entities.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GraphAPITest {
    private static final Logger logger = LoggerFactory.getLogger(GraphAPITest.class);

    private Driver driver;


    @BeforeEach
    void setUp() {
        driver = new Driver
                .Builder()
                .build();
        var graph = driver.graph("falkordb");
        graph.delete();
    }

    @AfterEach
    void tearDown() {
        driver.close();
    }


    @Test
    void testCreateNode() {
        var graph = driver.graph("falkordb");
        var query = "CREATE ({name:'roi',age:32})";
        QueryResult queryResult = graph.executeQuery(query);

        assertEquals(1, queryResult.getStatistics().nodesCreated());
        assertTrue(queryResult.getStatistics().getStringValue(Statistics.Label.NODES_DELETED).isEmpty());
        assertTrue(queryResult.getStatistics().getStringValue(Statistics.Label.RELATIONSHIPS_CREATED).isEmpty());
        assertTrue(queryResult.getStatistics().getStringValue(Statistics.Label.RELATIONSHIPS_DELETED).isEmpty());
        assertEquals(2, queryResult.getStatistics().propertiesSet());
        assertTrue(queryResult.getStatistics().getStringValue(Statistics.Label.QUERY_INTERNAL_EXECUTION_TIME).isPresent());
        var records = queryResult.records();
        assertTrue(records.isEmpty());
    }

    @Test
    void testCreateLabeledNode() {
        var graph = driver.graph("falkordb");
        var query = "CREATE (:human{name:'danny',age:12})";
        QueryResult queryResult = graph.executeQuery(query);

        assertEquals(1, queryResult.getStatistics().nodesCreated());
        assertEquals(2, queryResult.getStatistics().propertiesSet());
        assertTrue(queryResult.getStatistics().getStringValue(Statistics.Label.QUERY_INTERNAL_EXECUTION_TIME).isPresent());
    }

    @Test
    void testConnectNodes() {
        var graph = driver.graph("falkordb");

        // Create both source and destination nodes
        QueryResult result1 = graph.executeQuery("CREATE (:person{name:'roi',age:32})");
        QueryResult result2 = graph.executeQuery("CREATE (:person{name:'amit',age:30})");

        assertNotNull(result1);
        assertNotNull(result2);

        // Connect source and destination nodes
        QueryResult resultSet = graph.executeQuery(
                "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit') CREATE (a)-[:knows]->(b)");
        logger.info("statistics: {}", resultSet.getStatistics());
        assertTrue(resultSet.records().isEmpty());
        assertEquals(0, resultSet.getStatistics().nodesCreated());
        assertEquals(0, resultSet.getStatistics().propertiesSet());
        assertEquals(1, resultSet.getStatistics().relationshipsCreated());
        assertEquals(0, resultSet.getStatistics().relationshipsDeleted());
        assertTrue(resultSet.getStatistics().getStringValue(Statistics.Label.QUERY_INTERNAL_EXECUTION_TIME).isPresent());
    }

    @Test
    void testDeleteNodes() {
        var graph = driver.graph("falkordb");

        // Create nodes
        QueryResult result1 = graph.executeQuery("CREATE (:person{name:'roi',age:32})");
        QueryResult result2 = graph.executeQuery("CREATE (:person{name:'amit',age:30})");

        assertNotNull(result1);
        assertNotNull(result2);

        // Delete a node
        QueryResult deleteResult = graph.executeQuery("MATCH (a:person) WHERE (a.name = 'roi') DELETE a");

        assertTrue(deleteResult.records().isEmpty());
        assertEquals(0, deleteResult.getStatistics().nodesCreated());
        assertEquals(0, deleteResult.getStatistics().propertiesSet());
        assertEquals(0, deleteResult.getStatistics().relationshipsCreated());
        assertEquals(0, deleteResult.getStatistics().relationshipsDeleted());
        assertEquals(1, deleteResult.getStatistics().nodesDeleted());
        assertTrue(deleteResult.getStatistics().getInternalExecutionTime().isPresent());

        // Recreate and connect nodes
        result1 = graph.executeQuery("CREATE (:person{name:'roi',age:32})");
        assertNotNull(result1);
        QueryResult result3 = graph.executeQuery(
                "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit') CREATE (a)-[:knows]->(b)");
        assertNotNull(result3);

        // Delete a node again
        deleteResult = graph.executeQuery("MATCH (a:person) WHERE (a.name = 'roi') DELETE a");

        assertTrue(deleteResult.records().isEmpty());
        assertEquals(0, deleteResult.getStatistics().nodesCreated());
        assertEquals(0, deleteResult.getStatistics().propertiesSet());
        assertEquals(0, deleteResult.getStatistics().relationshipsCreated());
        assertEquals(1, deleteResult.getStatistics().relationshipsDeleted());
        assertEquals(1, deleteResult.getStatistics().nodesDeleted());
        assertTrue(deleteResult.getStatistics().getInternalExecutionTime().isPresent());
    }

    @Test
    void testDeleteRelationship() {
        var graph = driver.graph("falkordb");

        // Create nodes and relationship
        QueryResult result1 = graph.executeQuery("CREATE (:person{name:'roi',age:32})");
        QueryResult result2 = graph.executeQuery("CREATE (:person{name:'amit',age:30})");
        QueryResult result3 = graph.executeQuery(
                "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit') CREATE (a)-[:knows]->(a)");

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);

        // Delete the relationship
        QueryResult deleteResult = graph.executeQuery("MATCH (a:person)-[e]->() WHERE (a.name = 'roi') DELETE e");

        assertTrue(deleteResult.records().isEmpty());
        assertEquals(0, deleteResult.getStatistics().nodesCreated());
        assertEquals(0, deleteResult.getStatistics().propertiesSet());
        assertEquals(0, deleteResult.getStatistics().relationshipsCreated());
        assertEquals(0, deleteResult.getStatistics().nodesDeleted());
        assertEquals(1, deleteResult.getStatistics().relationshipsDeleted());
        assertTrue(deleteResult.getStatistics().getInternalExecutionTime().isPresent());
    }

    @Test
    void testIndex() {
        var graph = driver.graph("falkordb");

        // Create both source and destination nodes
        QueryResult result1 = graph.executeQuery("CREATE (:person{name:'roi',age:32})");
        assertNotNull(result1);

        QueryResult createIndexResult = graph.executeQuery("CREATE INDEX ON :person(age)");
        assertTrue(createIndexResult.records().isEmpty());
        assertEquals(1, createIndexResult.getStatistics().indicesAdded());

        // since RediSearch has index, those actions are allowed
        QueryResult createNonExistingIndexResult = graph.executeQuery("CREATE INDEX ON :person(age1)");
        assertTrue(createNonExistingIndexResult.records().isEmpty());
        assertNotNull(createNonExistingIndexResult.getStatistics().getStringValue(Statistics.Label.INDICES_ADDED));
        assertEquals(1, createNonExistingIndexResult.getStatistics().indicesAdded());

        try {
            graph.executeQuery("CREATE INDEX ON :person(age)");
            fail("Expected Exception was not thrown.");
        } catch (Exception e) {
            // Expected exception
        }

        QueryResult deleteExistingIndexResult = graph.executeQuery("DROP INDEX ON :person(age)");
        assertTrue(deleteExistingIndexResult.records().isEmpty());
        assertNotNull(deleteExistingIndexResult.getStatistics().getStringValue(Statistics.Label.INDICES_DELETED));
        assertEquals(1, deleteExistingIndexResult.getStatistics().indicesDeleted());
    }

    @Test
    public void testHeader() {
        var graph = driver.graph("falkordb");

        assertNotNull(graph.executeQuery("CREATE (:person{name:'roi',age:32})"));
        assertNotNull(graph.executeQuery("CREATE (:person{name:'amit',age:30})"));
        assertNotNull(graph.executeQuery(
                "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit') CREATE (a)-[:knows]->(a)"));

        QueryResult queryResult = graph.executeQuery("MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, a.age");

        Header header = queryResult.getHeader();
        assertNotNull(header);
        assertEquals("Header{"
                + "schemaTypes=[COLUMN_SCALAR, COLUMN_SCALAR, COLUMN_SCALAR], "
                + "schemaNames=[a, r, a.age]}", header.toString());

        List<String> schemaNames = header.getSchemaNames();

        assertNotNull(schemaNames);
        assertEquals(3, schemaNames.size());
        assertEquals("a", schemaNames.get(0));
        assertEquals("r", schemaNames.get(1));
        assertEquals("a.age", schemaNames.get(2));
    }

    @Test
    public void testRecord() {
        String name = "roi";
        int age = 32;
        double doubleValue = 3.14;
        boolean boolValue = true;

        String place = "TLV";
        int since = 2000;

        Property<String> nameProperty = new Property<>("name", name);
        Property<Integer> ageProperty = new Property<>("age", age);
        Property<Double> doubleProperty = new Property<>("doubleValue", doubleValue);
        Property<Boolean> trueBooleanProperty = new Property<>("boolValue", boolValue);
        Property<Boolean> falseBooleanProperty = new Property<>("boolValue", false);

        Map<String, Property<?>> nodeProperties = new HashMap<>();
        nodeProperties.put("name", nameProperty);
        nodeProperties.put("age", ageProperty);
        nodeProperties.put("doubleValue", doubleProperty);
        nodeProperties.put("boolValue", trueBooleanProperty);

        Node expectedNode = new Node(0, nodeProperties, Collections.singletonList("person"));

        assertEquals(
                "Node{labels=[person], id=0, "
                        + "propertyMap={name=Property{name='name', value=roi}, "
                        + "boolValue=Property{name='boolValue', value=true}, "
                        + "doubleValue=Property{name='doubleValue', value=3.14}, "
                        + "age=Property{name='age', value=32}}}",
                expectedNode.toString());
        assertEquals(4, expectedNode.getNumberOfProperties());

        Map<String, Property<?>> propertyMap = new HashMap<>();
        propertyMap.put("place", new Property<>("place", place));
        propertyMap.put("since", new Property<>("since", since));
        propertyMap.put("doubleValue", doubleProperty);
        propertyMap.put("boolValue", falseBooleanProperty);

        Edge expectedEdge = new Edge(propertyMap, "knows", 0, 1);
        expectedEdge.setId(0);

        assertEquals("Edge{relationshipType='knows', source=0, destination=1, id=0, "
                + "propertyMap={boolValue=Property{name='boolValue', value=false}, "
                + "place=Property{name='place', value=TLV}, "
                + "doubleValue=Property{name='doubleValue', value=3.14}, "
                + "since=Property{name='since', value=2000}}}", expectedEdge.toString());
        assertEquals(4, expectedEdge.getNumberOfProperties());

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("age", age);
        params.put("boolValue", boolValue);
        params.put("doubleValue", doubleValue);

        var graph = driver.graph("falkordb");
        assertNotNull(graph.executeQuery(
                "CREATE (:person{name:$name,age:$age, doubleValue:$doubleValue, boolValue:$boolValue})", params));
        assertNotNull(graph.executeQuery("CREATE (:person{name:'amit',age:30})"));
        assertNotNull(
                graph.executeQuery("MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  " +
                        "CREATE (a)-[:knows{place:'TLV', since:2000,doubleValue:3.14, boolValue:false}]->(b)"));

        QueryResult resultSet = graph.executeQuery("MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, " +
                "a.name, a.age, a.doubleValue, a.boolValue, " +
                "r.place, r.since, r.doubleValue, r.boolValue");
        assertNotNull(resultSet);

        assertEquals(0, resultSet.getStatistics().nodesCreated());
        assertEquals(0, resultSet.getStatistics().nodesDeleted());
        assertEquals(0, resultSet.getStatistics().labelsAdded());
        assertEquals(0, resultSet.getStatistics().propertiesSet());
        assertEquals(0, resultSet.getStatistics().relationshipsCreated());
        assertEquals(0, resultSet.getStatistics().relationshipsDeleted());
        assertTrue(resultSet.getStatistics().getInternalExecutionTime().isPresent());

        assertEquals(1, resultSet.records().size());

        Iterator<Record> iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        Record record = iterator.next();
        assertFalse(iterator.hasNext());

        Node node = record.getValue(0);
        assertNotNull(node);

        assertEquals(expectedNode, node);

        node = record.getValue("a");
        assertEquals(expectedNode, node);

        Edge edge = record.getValue(1);
        assertNotNull(edge);
        assertEquals(expectedEdge, edge);

        edge = record.getValue("r");
        assertEquals(expectedEdge, edge);

        assertEquals(Arrays.asList("a", "r", "a.name", "a.age", "a.doubleValue", "a.boolValue",
                "r.place", "r.since", "r.doubleValue", "r.boolValue"), record.keys());

        assertEquals(Arrays.asList(expectedNode, expectedEdge,
                        name, (long) age, doubleValue, true,
                        place, (long) since, doubleValue, false),
                record.values());

        Node a = record.getValue("a");
        for (String propertyName : expectedNode.getEntityPropertyNames()) {
            assertEquals(expectedNode.getProperty(propertyName), a.getProperty(propertyName));
        }

        assertEquals("roi", record.getString(2));
        assertEquals("32", record.getString(3));
        assertEquals(32L, ((Long) record.getValue(3)).longValue());
        assertEquals(32L, ((Long) record.getValue("a.age")).longValue());
        assertEquals("roi", record.getString("a.name"));
        assertEquals("32", record.getString("a.age"));
    }


    @Test
    public void testMultiThread() {
        var graph = driver.graph("falkordb");

        assertNotNull(
                graph.executeQuery("CREATE (:person {name:'roi', age:32})-[:knows]->(:person {name:'amit',age:30}) "));

        List<QueryResult> resultSets = IntStream.range(0, 16).parallel()
                .mapToObj(i -> graph.executeQuery("MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, a.age"))
                .collect(Collectors.toList());

        Property<String> nameProperty = new Property<>("name", "roi");
        Property<Integer> ageProperty = new Property<>("age", 32);
        Property<String> lastNameProperty = new Property<>("lastName", "a");

        Map<String, Property<?>> nodeProperties = new HashMap<>();
        nodeProperties.put("name", nameProperty);
        nodeProperties.put("age", ageProperty);

        Node expectedNode = new Node(0, nodeProperties, Collections.singletonList("person"));

        Map<String, Property<?>> edgeProperties = new HashMap<>();
        Edge expectedEdge = new Edge(edgeProperties, "knows", 0, 1);
        expectedEdge.setId(0);

        for (QueryResult resultSet : resultSets) {
            assertNotNull(resultSet.getHeader());
            Header header = resultSet.getHeader();
            List<String> schemaNames = header.getSchemaNames();
            assertNotNull(schemaNames);
            assertEquals(3, schemaNames.size());
            assertEquals("a", schemaNames.get(0));
            assertEquals("r", schemaNames.get(1));
            assertEquals("a.age", schemaNames.get(2));
            assertEquals(1, resultSet.records().size());

            Iterator<Record> iterator = resultSet.records().iterator();
            assertTrue(iterator.hasNext());
            Record record = iterator.next();
            assertFalse(iterator.hasNext());
            assertEquals(Arrays.asList("a", "r", "a.age"), record.keys());
            assertEquals(Arrays.asList(expectedNode, expectedEdge, 32L), record.values());
        }

        // test for update in local cache
        expectedNode.removeProperty("name");
        expectedNode.removeProperty("age");
        expectedNode.addProperty(lastNameProperty);
        expectedNode.removeLabel("person");
        expectedNode.addLabel("worker");
        expectedNode.setId(2);

        Map<String, Property<?>> updatedEdgeProperties = new HashMap<>();
        expectedEdge = new Edge(updatedEdgeProperties, "worksWith", 2, 3);
        expectedEdge.setId(1);

        assertNotNull(graph.executeQuery("CREATE (:worker{lastName:'a'})"));
        assertNotNull(graph.executeQuery("CREATE (:worker{lastName:'b'})"));
        assertNotNull(graph.executeQuery(
                "MATCH (a:worker), (b:worker) WHERE (a.lastName = 'a' AND b.lastName='b')  CREATE (a)-[:worksWith]->(b)"));

        resultSets = IntStream.range(0, 16).parallel()
                .mapToObj(i -> graph.executeQuery("MATCH (a:worker)-[r:worksWith]->(b:worker) RETURN a,r"))
                .toList();

        for (QueryResult resultSet : resultSets) {
            assertNotNull(resultSet.getHeader());
            Header header = resultSet.getHeader();
            List<String> schemaNames = header.getSchemaNames();
            assertNotNull(schemaNames);
            assertEquals(2, schemaNames.size());
            assertEquals("a", schemaNames.get(0));
            assertEquals("r", schemaNames.get(1));
            assertEquals(1, resultSet.records().size());

            Iterator<Record> iterator = resultSet.records().iterator();
            assertTrue(iterator.hasNext());
            Record record = iterator.next();
            assertFalse(iterator.hasNext());
            assertEquals(Arrays.asList("a", "r"), record.keys());
            assertEquals(Arrays.asList(expectedNode, expectedEdge), record.values());
        }
    }

    @Test
    public void testAdditionToProcedures() {
        var graph = driver.graph("falkordb");

        assertNotNull(graph.executeQuery("CREATE (:person{name:'roi',age:32})"));
        assertNotNull(graph.executeQuery("CREATE (:person{name:'amit',age:30})"));
        assertNotNull(graph.executeQuery(
                "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(b)"));

        // expected objects init
        Property<String> nameProperty = new Property<>("name", "roi");
        Property<Integer> ageProperty = new Property<>("age", 32);
        Property<String> lastNameProperty = new Property<>("lastName", "a");

        Map<String, Property<?>> nodeProperties = new HashMap<>();
        nodeProperties.put("name", nameProperty);
        nodeProperties.put("age", ageProperty);

        Node expectedNode = new Node(0, nodeProperties, Collections.singletonList("person"));

        Map<String, Property<?>> edgeProperties = new HashMap<>();
        Edge expectedEdge = new Edge(edgeProperties, "knows", 0, 1);
        expectedEdge.setId(0);

        QueryResult resultSet = graph.executeQuery("MATCH (a:person)-[r:knows]->(b:person) RETURN a,r");
        assertNotNull(resultSet.getHeader());
        Header header = resultSet.getHeader();
        List<String> schemaNames = header.getSchemaNames();
        assertNotNull(schemaNames);
        assertEquals(2, schemaNames.size());
        assertEquals("a", schemaNames.get(0));
        assertEquals("r", schemaNames.get(1));
        assertEquals(1, resultSet.records().size());

        Iterator<Record> iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        Record record = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals(Arrays.asList("a", "r"), record.keys());
        assertEquals(Arrays.asList(expectedNode, expectedEdge), record.values());

        // test for local cache updates
        expectedNode.removeProperty("name");
        expectedNode.removeProperty("age");
        expectedNode.addProperty(lastNameProperty);
        expectedNode.removeLabel("person");
        expectedNode.addLabel("worker");
        expectedNode.setId(2);

        Map<String, Property<?>> updatedEdgeProperties = new HashMap<>();
        expectedEdge = new Edge(updatedEdgeProperties, "worksWith", 2, 3);
        expectedEdge.setId(1);

        assertNotNull(graph.executeQuery("CREATE (:worker{lastName:'a'})"));
        assertNotNull(graph.executeQuery("CREATE (:worker{lastName:'b'})"));
        assertNotNull(graph.executeQuery(
                "MATCH (a:worker), (b:worker) WHERE (a.lastName = 'a' AND b.lastName='b')  CREATE (a)-[:worksWith]->(b)"));
        resultSet = graph.executeQuery("MATCH (a:worker)-[r:worksWith]->(b:worker) RETURN a,r");
        assertNotNull(resultSet.getHeader());
        header = resultSet.getHeader();
        schemaNames = header.getSchemaNames();
        assertNotNull(schemaNames);
        assertEquals(2, schemaNames.size());
        assertEquals("a", schemaNames.get(0));
        assertEquals("r", schemaNames.get(1));
        assertEquals(1, resultSet.records().size());

        iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        record = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals(Arrays.asList("a", "r"), record.keys());
        assertEquals(Arrays.asList(expectedNode, expectedEdge), record.values());
    }

    @Test
    public void testEscapedQuery() {
        var graph = driver.graph("falkordb");

        Map<String, Object> params1 = new HashMap<>();
        params1.put("s1", "S\"'");
        params1.put("s2", "S'\"");
        assertNotNull(graph.executeQuery("CREATE (:escaped{s1:$s1,s2:$s2})", params1));

        Map<String, Object> params2 = new HashMap<>();
        params2.put("s1", "S\"'");
        params2.put("s2", "S'\"");
        assertNotNull(graph.executeQuery("MATCH (n) where n.s1=$s1 and n.s2=$s2 RETURN n", params2));

        assertNotNull(graph.executeQuery("MATCH (n) where n.s1='S\"' RETURN n"));
    }

    @Test
    public void testContextedAPI() {
        String name = "roi";
        int age = 32;
        double doubleValue = 3.14;
        boolean boolValue = true;

        String place = "TLV";
        int since = 2000;

        Property<String> nameProperty = new Property<>("name", name);
        Property<Integer> ageProperty = new Property<>("age", age);
        Property<Double> doubleProperty = new Property<>("doubleValue", doubleValue);
        Property<Boolean> trueBooleanProperty = new Property<>("boolValue", true);
        Property<Boolean> falseBooleanProperty = new Property<>("boolValue", false);

        Property<String> placeProperty = new Property<>("place", place);
        Property<Integer> sinceProperty = new Property<>("since", since);

        Map<String, Property<?>> nodeProperties = new HashMap<>();
        nodeProperties.put("name", nameProperty);
        nodeProperties.put("age", ageProperty);
        nodeProperties.put("doubleValue", doubleProperty);
        nodeProperties.put("boolValue", trueBooleanProperty);

        Node expectedNode = new Node(0, nodeProperties, Collections.singletonList("person"));

        Map<String, Property<?>> edgeProperties = new HashMap<>();
        edgeProperties.put("place", placeProperty);
        edgeProperties.put("since", sinceProperty);
        edgeProperties.put("doubleValue", doubleProperty);
        edgeProperties.put("boolValue", falseBooleanProperty);

        Edge expectedEdge = new Edge(edgeProperties, "knows", 0, 1);
        expectedEdge.setId(0);

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("age", age);
        params.put("boolValue", boolValue);
        params.put("doubleValue", doubleValue);

        var graph = driver.graph("falkordb");
        assertNotNull(graph.executeQuery(
                "CREATE (:person{name:$name, age:$age, doubleValue:$doubleValue, boolValue:$boolValue})", params));
        assertNotNull(graph.executeQuery("CREATE (:person{name:'amit',age:30})"));
        assertNotNull(graph.executeQuery(
                "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  " +
                        "CREATE (a)-[:knows{place:'TLV', since:2000,doubleValue:3.14, boolValue:false}]->(b)"));

        QueryResult resultSet = graph.executeQuery("MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, " +
                "a.name, a.age, a.doubleValue, a.boolValue, " +
                "r.place, r.since, r.doubleValue, r.boolValue");
        assertNotNull(resultSet);

        assertEquals(0, resultSet.getStatistics().nodesCreated());
        assertEquals(0, resultSet.getStatistics().nodesDeleted());
        assertEquals(0, resultSet.getStatistics().labelsAdded());
        assertEquals(0, resultSet.getStatistics().propertiesSet());
        assertEquals(0, resultSet.getStatistics().relationshipsCreated());
        assertEquals(0, resultSet.getStatistics().relationshipsDeleted());
        assertTrue(resultSet.getStatistics().getInternalExecutionTime().isPresent());

        assertEquals(1, resultSet.records().size());

        Iterator<Record> iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        Record record = iterator.next();
        assertFalse(iterator.hasNext());

        Node node = record.getValue(0);
        assertNotNull(node);
        assertEquals(expectedNode, node);

        node = record.getValue("a");
        assertEquals(expectedNode, node);

        Edge edge = record.getValue(1);
        assertNotNull(edge);
        assertEquals(expectedEdge, edge);

        edge = record.getValue("r");
        assertEquals(expectedEdge, edge);

        assertEquals(Arrays.asList("a", "r", "a.name", "a.age", "a.doubleValue", "a.boolValue",
                "r.place", "r.since", "r.doubleValue", "r.boolValue"), record.keys());

        assertEquals(Arrays.asList(expectedNode, expectedEdge,
                name, (long) age, doubleValue, true,
                place, (long) since, doubleValue, false), record.values());

        Node a = record.getValue("a");
        for (String propertyName : expectedNode.getEntityPropertyNames()) {
            assertEquals(expectedNode.getProperty(propertyName), a.getProperty(propertyName));
        }

        assertEquals("roi", record.getString(2));
        assertEquals("32", record.getString(3));
        assertEquals(32L, ((Long) record.getValue(3)).longValue());
        assertEquals(32L, ((Long) record.getValue("a.age")).longValue());
        assertEquals("roi", record.getString("a.name"));
        assertEquals("32", record.getString("a.age"));
    }

    @Test
    public void testArraySupport() {
        var graph = driver.graph("falkordb");

        Node expectedANode = new Node(0, Map.of(
                "name", new Property<>("name", "a"),
                "age", new Property<>("age", 32),
                "array", new Property<>("array", Arrays.asList(0L, 1L, 2L))
        ), Collections.singletonList("person"));

        Node expectedBNode = new Node(1, Map.of(
                "name", new Property<>("name", "b"),
                "age", new Property<>("age", 30),
                "array", new Property<>("array", Arrays.asList(3L, 4L, 5L))
        ), Collections.singletonList("person"));

        assertNotNull(graph.executeQuery("CREATE (:person{name:'a',age:32,array:[0,1,2]})"));
        assertNotNull(graph.executeQuery("CREATE (:person{name:'b',age:30,array:[3,4,5]})"));

        // test array
        QueryResult resultSet = graph.executeQuery("WITH [0,1,2] as x return x");

        // check header
        assertNotNull(resultSet.getHeader());
        Header header = resultSet.getHeader();

        List<String> schemaNames = header.getSchemaNames();
        assertNotNull(schemaNames);
        assertEquals(1, schemaNames.size());
        assertEquals("x", schemaNames.get(0));

        // check record
        assertEquals(1, resultSet.records().size());

        Iterator<Record> iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        Record record = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals(Arrays.asList("x"), record.keys());

        List<Long> x = record.getValue("x");
        assertEquals(Arrays.asList(0L, 1L, 2L), x);

        // test collect
        resultSet = graph.executeQuery("MATCH(n) return collect(n) as x");

        assertNotNull(resultSet.getHeader());
        header = resultSet.getHeader();

        schemaNames = header.getSchemaNames();
        assertNotNull(schemaNames);
        assertEquals(1, schemaNames.size());
        assertEquals("x", schemaNames.get(0));

        // check record
        assertEquals(1, resultSet.records().size());

        iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        record = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals(List.of("x"), record.keys());
        x = record.getValue("x");
        assertEquals(Arrays.asList(expectedANode, expectedBNode), x);

        // test unwind
        resultSet = graph.executeQuery("unwind([0,1,2]) as x return x");

        assertNotNull(resultSet.getHeader());
        header = resultSet.getHeader();

        schemaNames = header.getSchemaNames();
        assertNotNull(schemaNames);
        assertEquals(1, schemaNames.size());
        assertEquals("x", schemaNames.get(0));

        // check record
        assertEquals(3, resultSet.records().size());

        iterator = resultSet.records().iterator();
        for (long i = 0; i < 3; i++) {
            assertTrue(iterator.hasNext());
            record = iterator.next();
            assertEquals(List.of("x"), record.keys());
            assertEquals(i, (long) record.getValue("x"));
        }
    }

    @Test
    public void testNullGraphEntities() {
        var graph = driver.graph("falkordb");

        // Create two nodes connected by a single outgoing edge.
        assertNotNull(graph.executeQuery("CREATE (:L)-[:E]->(:L2)"));

        // Test a query that produces 1 record with 3 null values.
        QueryResult resultSet = graph.executeQuery("OPTIONAL MATCH (a:NONEXISTENT)-[e]->(b) RETURN a, e, b");
        assertEquals(1, resultSet.records().size());

        Iterator<Record> iterator = resultSet.records().iterator();
        assertTrue(iterator.hasNext());
        Record record = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals(Arrays.asList(null, null, null), record.values());

        // Test a query that produces 2 records, with 2 null values in the second.
        resultSet = graph.executeQuery("MATCH (a) OPTIONAL MATCH (a)-[e]->(b) RETURN a, e, b ORDER BY ID(a)");
        assertEquals(2, resultSet.records().size());

        iterator = resultSet.records().iterator();
        record = iterator.next();
        assertEquals(3, record.size());

        assertNotNull(record.getValue(0));
        assertNotNull(record.getValue(1));
        assertNotNull(record.getValue(2));

        record = iterator.next();
        assertEquals(3, record.size());

        assertNotNull(record.getValue(0));
        assertNull(record.getValue(1));
        assertNull(record.getValue(2));

        // Test a query that produces 2 records, the first containing a path and the
        // second containing a null value.
        resultSet = graph.executeQuery("MATCH (a) OPTIONAL MATCH p = (a)-[e]->(b) RETURN p");
        assertEquals(2, resultSet.records().size());

        iterator = resultSet.records().iterator();
        record = iterator.next();
        assertEquals(1, record.size());
        assertNotNull(record.getValue(0));

        record = iterator.next();
        assertEquals(1, record.size());
        assertNull(record.getValue(0));
    }

    @Test
    public void test64bitNumber() {
        long value = 1L << 40;
        Map<String, Object> params = new HashMap<>();
        params.put("val", value);
        var graph = driver.graph("falkordb");
        QueryResult resultSet = graph.executeQuery("CREATE (n {val:$val}) RETURN n.val", params);
        assertEquals(1, resultSet.records().size());
        Record r = resultSet.records().iterator().next();
        assertEquals(Long.valueOf(value), r.getValue(0));
    }

    @Test
    public void testVecf32() {
        var graph = driver.graph("falkordb");
        QueryResult resultSet = graph.executeQuery("RETURN vecf32([2.1, -0.82, 1.3, 4.5]) AS vector");
        assertEquals(1, resultSet.records().size());
        Record r = resultSet.records().iterator().next();
        List<Object> vector = r.getValue(0);
        assertEquals(4, vector.size());
        Object res = vector.get(0);

        // The result can be either Double or Float depending on the server version
        if (res instanceof Double) {
            List<Double> v = r.getValue(0);
            assertEquals(2.1, v.get(0), 0.01);
            assertEquals(-0.82, v.get(1), 0.01);
            assertEquals(1.3, v.get(2), 0.01);
            assertEquals(4.5, v.get(3), 0.01);
        } else {
            List<Float> v = r.getValue(0);
            assertEquals(2.1f, v.get(0), 0.01);
            assertEquals(-0.82f, v.get(1), 0.01);
            assertEquals(1.3f, v.get(2), 0.01);
            assertEquals(4.5f, v.get(3), 0.01);
        }
    }

    @Test
    public void testCachedExecution() {
        var graph = driver.graph("falkordb");
        graph.executeQuery("CREATE (:N {val:1}), (:N {val:2})");

        // First time should not be loaded from execution cache
        Map<String, Object> params = new HashMap<>();
        params.put("val", 1L);
        QueryResult resultSet = graph.executeQuery("MATCH (n:N {val:$val}) RETURN n.val", params);
        assertEquals(1, resultSet.records().size());
        Record r = resultSet.records().iterator().next();
        assertEquals(params.get("val"), r.getValue(0));
        assertFalse(resultSet.getStatistics().cachedExecution());

        // Run in loop many times to make sure the query will be loaded
        // from cache at least once
        for (int i = 0; i < 64; i++) {
            resultSet = graph.executeQuery("MATCH (n:N {val:$val}) RETURN n.val", params);
        }
        assertEquals(1, resultSet.records().size());
        r = resultSet.records().iterator().next();
        assertEquals(params.get("val"), r.getValue(0));
        assertTrue(resultSet.getStatistics().cachedExecution());
    }

    @Test
    public void testMapDataType() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("a", (long) 1);
        expected.put("b", "str");
        expected.put("c", null);
        List<Object> d = new ArrayList<>();
        d.add((long) 1);
        d.add((long) 2);
        d.add((long) 3);
        expected.put("d", d);
        expected.put("e", true);
        Map<String, Object> f = new HashMap<>();
        f.put("x", (long) 1);
        f.put("y", (long) 2);
        expected.put("f", f);

        var graph = driver.graph("falkordb");
        QueryResult resultSet = graph.executeQuery("RETURN {a:1, b:'str', c:NULL, d:[1,2,3], e:True, f:{x:1, y:2}}");

        assertEquals(1, resultSet.records().size());
        Record r = resultSet.records().iterator().next();
        Map<String, Object> actual = r.getValue(0);
        assertEquals(expected, actual);
    }

    @Test
    public void testGeoPointLatLon() {
        var graph = driver.graph("falkordb");
        QueryResult resultSet = graph.executeQuery("CREATE (:restaurant"
                + " {location: point({latitude:30.27822306, longitude:-97.75134723})})");
        assertEquals(1, resultSet.getStatistics().nodesCreated());
        assertEquals(1, resultSet.getStatistics().propertiesSet());

        assertTestGeoPoint();
    }

    private void assertTestGeoPoint() {
        var graph = driver.graph("falkordb");
        QueryResult results = graph.executeQuery("MATCH (restaurant) RETURN restaurant");
        assertEquals(1, results.records().size());
        Record record = results.records().iterator().next();
        assertEquals(1, record.size());
        assertEquals(Collections.singletonList("restaurant"), record.keys());
        Node node = record.getValue(0);
        Property<?> property = node.getProperty("location");
        Point result = (Point) property.value();

        Point point = new Point(30.27822306, -97.75134723);
        assertEquals(point, result);
        assertEquals(30.27822306, result.getLatitude(), 0.01);
        assertEquals(-97.75134723, result.getLongitude(), 0.01);
        assertEquals("Point{latitude=30.2782230377197, longitude=-97.751350402832}", result.toString());
        assertEquals(-132320535, result.hashCode());
    }
}

