package app.engine.command;

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

class AddVehicleCommandTest {

    @Test
    void execute_AddsVehicleToCorrectLaneAndNotifiesObservers() {
        ArrayList<TrafficLane> lanes = new ArrayList<>();
        ArrayList<Movement> snMovements = new ArrayList<>(List.of(new Movement(Direction.SOUTH, Direction.NORTH)));
        TrafficLane snLane = new TrafficLane(Direction.SOUTH, snMovements);
        lanes.add(snLane);
        
        TestIntersection intersection = new TestIntersection(lanes);

        AddVehicleCommand command = new AddVehicleCommand("v1", Direction.SOUTH, Direction.NORTH, intersection);
        command.execute();

        assertEquals(1, snLane.getVehiclesCount());
        Vehicle capturedVehicle = snLane.getVehicles().getFirst();
        assertEquals("v1", capturedVehicle.getId());
        assertEquals(Direction.SOUTH, capturedVehicle.getStartRoad());
        assertEquals(Direction.NORTH, capturedVehicle.getEndRoad());
        
        assertTrue(intersection.notifiedArrival);
        assertTrue(intersection.notifiedObservers);
    }

    private static class TestIntersection extends Intersection {
        boolean notifiedArrival = false;
        boolean notifiedObservers = false;

        public TestIntersection(ArrayList<TrafficLane> trafficLanes) {
            super(trafficLanes, 10, true);
        }

        @Override
        public void notifyArrival(String vehicleId, Direction from, Direction to) {
            if ("v1".equals(vehicleId) && from == Direction.SOUTH && to == Direction.NORTH) {
                notifiedArrival = true;
            }
        }

        @Override
        public void notifyObservers() {
            notifiedObservers = true;
        }
    }
}
