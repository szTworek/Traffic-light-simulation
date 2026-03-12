package app;

import app.io.InputParser;
import app.io.InvalidInputException;
import app.io.SimulationReport;
import app.model.Specification;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Two arguments required: <input_file> <output_file>");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args[1];

        if (!new File(inputPath).exists()) {
            System.err.println("Input file not found: " + inputPath);
            System.exit(1);
        }

        try {
            SimulationReport report = new SimulationReport();
            InputParser parser = new InputParser(inputPath, report);
            Specification spec = parser.parse();

            Simulation simulation = new Simulation(spec, report, outputPath);
            simulation.start();
        } catch (InvalidInputException e) {
            System.err.println("Input error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}
