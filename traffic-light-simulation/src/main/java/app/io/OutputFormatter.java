package app.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OutputFormatter {

    public static void write(SimulationReport report, String outputPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode root = mapper.createObjectNode();
        ArrayNode stepStatusesNode = root.putArray("stepStatuses");

        for (List<String> stepVehicles : report.getStepStatuses()) {
            ObjectNode stepNode = mapper.createObjectNode();
            ArrayNode leftVehiclesNode = stepNode.putArray("leftVehicles");
            for (String vehicleId : stepVehicles) {
                leftVehiclesNode.add(vehicleId);
            }
            stepStatusesNode.add(stepNode);
        }

        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            System.err.println("Warning: directory does not exist: " + parentDir.getPath()
                    + ". Writing to " + outputFile.getName());
            outputFile = new File(outputFile.getName());
        }

        mapper.writeValue(outputFile, root);
    }
}
