package app.model;

import app.engine.observer.IntersectionObserver;
import app.engine.observer.IntersectionSnapshot;
import app.engine.observer.VehicleArrivalListener;
import app.engine.observer.VehicleDepartureListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {

    private Intersection intersection;
    private ArrayList<TrafficLane> lanes;

    @BeforeEach
    void setUp() {
        lanes = new ArrayList<>();
        lanes.add(new TrafficLane(Direction.SOUTH, new ArrayList<>(List.of(new Movement(Direction.SOUTH, Direction.NORTH)))));
        lanes.add(new TrafficLane(Direction.NORTH, new ArrayList<>(List.of(new Movement(Direction.NORTH, Direction.SOUTH)))));
        
        intersection = new Intersection(lanes, 10, true);
    }

    @Test
    void getTrafficLane_ExistingMovement_ReturnsCorrectLane() {
        TrafficLane lane = intersection.getTrafficLane(Direction.SOUTH, Direction.NORTH);
        assertNotNull(lane);
        assertEquals(Direction.SOUTH, lane.getDirection());
        assertTrue(lane.getPossibleMovements().contains(new Movement(Direction.SOUTH, Direction.NORTH)));
    }

    @Test
    void getTrafficLane_NonExistingMovement_ThrowsIllegalStateException() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            intersection.getTrafficLane(Direction.SOUTH, Direction.EAST);
        });
        assertTrue(exception.getMessage().contains("No traffic lane found for movement"));
    }

    @Test
    void notifyArrival_NotifiesAllListeners() {
        TestArrivalListener listener = new TestArrivalListener();
        intersection.addArrivalListener(listener);

        intersection.notifyArrival("v1", Direction.SOUTH, Direction.NORTH);

        assertEquals(1, listener.callCount);
        assertEquals("v1", listener.lastId);
        assertEquals(Direction.SOUTH, listener.lastFrom);
        assertEquals(Direction.NORTH, listener.lastTo);
    }

    @Test
    void notifyDeparture_NotifiesAllListeners() {
        TestDepartureListener listener = new TestDepartureListener();
        intersection.addDepartureListener(listener);

        intersection.notifyDeparture("v1", Direction.SOUTH, Direction.NORTH);

        assertEquals(1, listener.callCount);
        assertEquals("v1", listener.lastId);
        assertEquals(Direction.SOUTH, listener.lastFrom);
        assertEquals(Direction.NORTH, listener.lastTo);
    }

    @Test
    void notifyObservers_NotifiesAllObservers() {
        TestObserver observer = new TestObserver();
        intersection.addObserver(observer);

        intersection.notifyObservers();
        assertEquals(1, observer.callCount);
        
        intersection.removeObserver(observer);
        intersection.notifyObservers();
        
        assertEquals(1, observer.callCount);
    }

    private static class TestArrivalListener implements VehicleArrivalListener {
        int callCount = 0;
        String lastId;
        Direction lastFrom;
        Direction lastTo;

        @Override
        public void onVehicleArrived(String vehicleId, Direction from, Direction to) {
            callCount++;
            lastId = vehicleId;
            lastFrom = from;
            lastTo = to;
        }
    }

    private static class TestDepartureListener implements VehicleDepartureListener {
        int callCount = 0;
        String lastId;
        Direction lastFrom;
        Direction lastTo;

        @Override
        public void onVehicleDeparted(String vehicleId, Direction from, Direction to) {
            callCount++;
            lastId = vehicleId;
            lastFrom = from;
            lastTo = to;
        }
    }

    private static class TestObserver implements IntersectionObserver {
        int callCount = 0;

        @Override
        public void onIntersectionChanged(IntersectionSnapshot snapshot) {
            callCount++;
        }
    }
}
