package app;

import app.io.InputParser;
import app.io.SimulationReport;
import app.model.Specification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationIntegrationTest {

    @Test
    void testCompleteSimulationFlow(@TempDir Path tempDir) throws IOException {
        String inputFilePath = "src/test/resources/test-input-1.json";
        Path outputFilePath = tempDir.resolve("output.json");

        SimulationReport report = new SimulationReport();
        InputParser parser = new InputParser(inputFilePath, report);
        Specification spec = parser.parse();

        Simulation simulation = new Simulation(spec, report, outputFilePath.toString());
        simulation.start();

        assertTrue(Files.exists(outputFilePath));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(outputFilePath.toString()));

        assertTrue(root.has("stepStatuses"));
        JsonNode stepStatuses = root.get("stepStatuses");
        
        assertEquals(2, stepStatuses.size());
        
        JsonNode step1 = stepStatuses.get(0);
        assertTrue(step1.has("leftVehicles"));
        assertEquals(1, step1.get("leftVehicles").size());
        assertEquals("v1", step1.get("leftVehicles").get(0).asText());

        JsonNode step2 = stepStatuses.get(1);
        assertTrue(step2.has("leftVehicles"));
        assertEquals(1, step2.get("leftVehicles").size());
        assertEquals("v2", step2.get("leftVehicles").get(0).asText());
    }

    @Test
    void testConflictingVehicles_PriorityResolution(@TempDir Path tempDir) throws IOException {
        Path outputFilePath = tempDir.resolve("output.json");

        SimulationReport report = new SimulationReport();
        InputParser parser = new InputParser("src/test/resources/test-input-conflict.json", report);
        Specification spec = parser.parse();

        new Simulation(spec, report, outputFilePath.toString()).start();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode stepStatuses = mapper.readTree(outputFilePath.toFile()).get("stepStatuses");

        assertEquals(2, stepStatuses.size());

        assertEquals(1, stepStatuses.get(0).get("leftVehicles").size());
        assertEquals(1, stepStatuses.get(1).get("leftVehicles").size());

        Set<String> departed = new HashSet<>();
        departed.add(stepStatuses.get(0).get("leftVehicles").get(0).asText());
        departed.add(stepStatuses.get(1).get("leftVehicles").get(0).asText());
        assertEquals(Set.of("v1", "v2"), departed);
    }

    @Test
    void testTurnLanes_VehiclesRouteCorrectly(@TempDir Path tempDir) throws IOException {
        Path outputFilePath = tempDir.resolve("output.json");

        SimulationReport report = new SimulationReport();
        InputParser parser = new InputParser("src/test/resources/test-input-turn-lanes.json", report);
        Specification spec = parser.parse();

        new Simulation(spec, report, outputFilePath.toString()).start();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode stepStatuses = mapper.readTree(outputFilePath.toFile()).get("stepStatuses");

        assertEquals(1, stepStatuses.size());

        JsonNode leftVehicles = stepStatuses.get(0).get("leftVehicles");
        assertEquals(2, leftVehicles.size());

        Set<String> departed = new HashSet<>();
        for (JsonNode v : leftVehicles) departed.add(v.asText());
        assertEquals(Set.of("v1", "v2"), departed);
    }
}
