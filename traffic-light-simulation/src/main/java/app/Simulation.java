package app;

import app.io.OutputFormatter;
import app.io.SimulationReport;
import app.model.Intersection;
import app.model.Specification;
import app.engine.command.Command;
import app.engine.observer.AsciiIntersectionObserver;
import app.engine.observer.VehicleStatusObserver;

import java.io.IOException;
import java.util.ArrayList;

public class Simulation {
    private final ArrayList<Command> commands;
    private final Intersection intersection;
    private final SimulationReport report;
    private final String outputPath;

    public Simulation(Specification spec, SimulationReport report, String outputPath) {
        this.commands = spec.commands();
        this.intersection = spec.intersection();
        this.report = report;
        this.outputPath = outputPath;
    }

    public void start() throws IOException {
        intersection.addObserver(new AsciiIntersectionObserver());
        intersection.addObserver(new VehicleStatusObserver());
        intersection.addArrivalListener((id, from, to) ->
                System.out.println("\033[33m" + id + " arrived, wants " + from + " -> " + to + "\033[0m\n"));
        intersection.addDepartureListener((id, from, to) ->
                System.out.println("\033[36m" + id + " went from " + from + " to " + to + "\033[0m\n"));

        for (Command command : commands) {
            command.execute();
        }

        OutputFormatter.write(report, outputPath);
    }
}
