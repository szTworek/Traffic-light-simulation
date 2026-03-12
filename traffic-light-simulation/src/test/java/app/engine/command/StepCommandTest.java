package app.engine.command;

import app.io.SimulationReport;
import app.model.Direction;
import app.model.Intersection;
import app.model.Movement;
import app.model.TrafficLane;
import app.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepCommandTest {

    @Test
    void execute_PerformsStepAndAddsToReport() {
        ArrayList<TrafficLane> lanes = new ArrayList<>();
        ArrayList<Movement> snMovements = new ArrayList<>(List.of(new Movement(Direction.SOUTH, Direction.NORTH)));
        TrafficLane snLane = new TrafficLane(Direction.SOUTH, snMovements);
        
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        snLane.addVehicle(v1);
        lanes.add(snLane);
        
        Intersection intersection = new Intersection(lanes, 10, true);
        SimulationReport report = new SimulationReport();
        
        StepCommand command = new StepCommand(intersection, report);
        
        command.execute();
        
        List<List<String>> reportStatuses = report.getStepStatuses();
        assertEquals(1, reportStatuses.size());
        
        List<String> step1Vehicles = reportStatuses.get(0);
        assertEquals(1, step1Vehicles.size());
        assertTrue(step1Vehicles.contains("v1"));
        
        assertEquals(0, snLane.getVehiclesCount());
    }
}
