package app.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputFormatterTest {

    @Test
    void write_ValidReport_CreatesCorrectJson(@TempDir Path tempDir) throws IOException {
        SimulationReport report = new SimulationReport();
        report.addStep(List.of("v1", "v2"));
        report.addStep(List.of("v3"));
        report.addStep(List.of());

        Path outputFile = tempDir.resolve("output.json");

        OutputFormatter.write(report, outputFile.toString());

        assertTrue(Files.exists(outputFile));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(outputFile.toFile());

        assertTrue(root.has("stepStatuses"));
        assertTrue(root.get("stepStatuses").isArray());
        assertEquals(3, root.get("stepStatuses").size());

        JsonNode step1 = root.get("stepStatuses").get(0);
        assertTrue(step1.has("leftVehicles"));
        assertEquals(2, step1.get("leftVehicles").size());
        assertEquals("v1", step1.get("leftVehicles").get(0).asText());
        assertEquals("v2", step1.get("leftVehicles").get(1).asText());

        JsonNode step2 = root.get("stepStatuses").get(1);
        assertEquals(1, step2.get("leftVehicles").size());
        assertEquals("v3", step2.get("leftVehicles").get(0).asText());

        JsonNode step3 = root.get("stepStatuses").get(2);
        assertEquals(0, step3.get("leftVehicles").size());
    }

    @Test
    void write_NonExistentDirectory_FallsBackToCurrentDirectory() throws IOException {
        SimulationReport report = new SimulationReport();
        report.addStep(List.of("v1"));

        String nonExistentDir = "non_existent_dir_12345";
        String fileName = "fallback_output.json";
        String outputPath = nonExistentDir + File.separator + fileName;

        OutputFormatter.write(report, outputPath);

        File fallbackFile = new File(fileName);
        assertTrue(fallbackFile.exists());

        fallbackFile.delete();
    }
}
