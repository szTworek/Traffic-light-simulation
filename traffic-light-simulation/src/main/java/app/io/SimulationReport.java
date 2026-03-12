package app.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationReport {
    private final List<List<String>> stepStatuses = new ArrayList<>();

    public void addStep(List<String> leftVehicles) {
        stepStatuses.add(List.copyOf(leftVehicles));
    }

    public List<List<String>> getStepStatuses() {
        return Collections.unmodifiableList(stepStatuses);
    }
}
