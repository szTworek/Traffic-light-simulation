package app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void constructor_ValidArguments_CreatesVehicle() {
        Vehicle vehicle = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        
        assertEquals("v1", vehicle.getId());
        assertEquals(Direction.SOUTH, vehicle.getStartRoad());
        assertEquals(Direction.NORTH, vehicle.getEndRoad());
        assertEquals(0, vehicle.getStayTime());
        
        Movement m = vehicle.getMovement();
        assertEquals(Direction.SOUTH, m.from());
        assertEquals(Direction.NORTH, m.to());
    }

    @Test
    void constructor_NullId_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new Vehicle(null, Direction.SOUTH, Direction.NORTH);
        });
    }

    @Test
    void constructor_NullStartRoad_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new Vehicle("v1", null, Direction.NORTH);
        });
    }

    @Test
    void constructor_NullEndRoad_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new Vehicle("v1", Direction.SOUTH, null);
        });
    }

    @Test
    void constructor_SameStartAndEndRoad_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("v1", Direction.SOUTH, Direction.SOUTH);
        });
    }

    @Test
    void incrementStayTime_IncreasesTimeByOne() {
        Vehicle vehicle = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        
        vehicle.incrementStayTime();
        assertEquals(1, vehicle.getStayTime());
        
        vehicle.incrementStayTime();
        assertEquals(2, vehicle.getStayTime());
    }
}
