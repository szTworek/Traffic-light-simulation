package app.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrafficLaneTest {

    private TrafficLane trafficLane;
    private ArrayList<Movement> possibleMovements;

    @BeforeEach
    void setUp() {
        possibleMovements = new ArrayList<>(List.of(new Movement(Direction.SOUTH, Direction.NORTH)));
        trafficLane = new TrafficLane(Direction.SOUTH, possibleMovements);
    }

    @Test
    void constructor_InitializesProperly() {
        assertEquals(Direction.SOUTH, trafficLane.getDirection());
        assertEquals(TrafficLightColour.RED, trafficLane.getTrafficLight().getColour());
        assertTrue(trafficLane.getVehicles().isEmpty());
        assertEquals(possibleMovements, trafficLane.getPossibleMovements());
    }

    @Test
    void addVehicle_IncreasesVehicleCount() {
        Vehicle v = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        trafficLane.addVehicle(v);

        assertEquals(1, trafficLane.getVehiclesCount());
        assertEquals(v, trafficLane.getVehicles().getFirst());
    }

    @Test
    void removeFirstVehicle_WhenNotEmpty_RemovesAndReturnsVehicle() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        Vehicle v2 = new Vehicle("v2", Direction.SOUTH, Direction.NORTH);
        trafficLane.addVehicle(v1);
        trafficLane.addVehicle(v2);

        Vehicle removed = trafficLane.removeFirstVehicle();

        assertEquals(v1, removed);
        assertEquals(1, trafficLane.getVehiclesCount());
        assertEquals(v2, trafficLane.getVehicles().getFirst());
    }

    @Test
    void removeFirstVehicle_WhenEmpty_ThrowsIllegalStateException() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            trafficLane.removeFirstVehicle();
        });
        assertTrue(exception.getMessage().contains("Cannot remove vehicle from empty lane"));
    }

    @Test
    void getStayTime_WhenEmpty_ReturnsZero() {
        assertEquals(0, trafficLane.getStayTime());
    }

    @Test
    void getStayTime_WhenNotEmpty_ReturnsFirstVehicleStayTime() {
        Vehicle v = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        v.incrementStayTime();
        v.incrementStayTime();
        trafficLane.addVehicle(v);

        assertEquals(2, trafficLane.getStayTime());
    }

    @Test
    void getTotalStayTime_ReturnsSumOfAllVehiclesStayTime() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        v1.incrementStayTime();
        
        Vehicle v2 = new Vehicle("v2", Direction.SOUTH, Direction.NORTH);
        v2.incrementStayTime();
        v2.incrementStayTime();
        
        trafficLane.addVehicle(v1);
        trafficLane.addVehicle(v2);

        assertEquals(3, trafficLane.getTotalStayTime());
    }
}
