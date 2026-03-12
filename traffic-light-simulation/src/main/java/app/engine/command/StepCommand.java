package app.engine.command;

import app.engine.Step;
import app.io.SimulationReport;
import app.model.Intersection;

import java.util.List;

public class StepCommand implements Command {
    private final Intersection intersection;
    private final SimulationReport report;

    public StepCommand(Intersection intersection, SimulationReport report) {
        this.intersection = intersection;
        this.report = report;
    }

    @Override
    public void execute() {
        Step step = new Step(intersection);
        List<String> departedVehicleIds = step.execute();
        report.addStep(departedVehicleIds);
    }
}
