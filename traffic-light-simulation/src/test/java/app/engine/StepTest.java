package app.engine;

import app.model.Direction;
import app.model.Intersection;
import app.model.Movement;
import app.model.TrafficLane;
import app.model.TrafficLightColour;
import app.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StepTest {

    private Intersection intersection;
    private TrafficLane snLane;
    private TrafficLane nsLane;
    private TrafficLane ewLane;
    private TrafficLane weLane;

    @BeforeEach
    void setUp() {
        snLane = lane(Direction.SOUTH, new Movement(Direction.SOUTH, Direction.NORTH));
        nsLane = lane(Direction.NORTH, new Movement(Direction.NORTH, Direction.SOUTH));
        ewLane = lane(Direction.EAST, new Movement(Direction.EAST, Direction.WEST));
        weLane = lane(Direction.WEST, new Movement(Direction.WEST, Direction.EAST));

        ArrayList<TrafficLane> lanes = new ArrayList<>(List.of(snLane, nsLane, ewLane, weLane));
        intersection = new Intersection(lanes, 3, true);
    }

    private static TrafficLane lane(Direction dir, Movement... movements) {
        return new TrafficLane(dir, new ArrayList<>(List.of(movements)));
    }

    private Step newStep() {
        return new Step(intersection);
    }


    @Test
    void execute_NoVehicles_ReturnsEmptyListAndSetsRedLights() {
        List<String> departed = newStep().execute();

        assertTrue(departed.isEmpty());
        for (TrafficLane l : List.of(snLane, nsLane, ewLane, weLane)) {
            assertEquals(TrafficLightColour.RED, l.getTrafficLight().getColour());
        }
    }

    @Test
    void execute_OneVehicle_DepartsAndSetsGreen() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));

        List<String> departed = newStep().execute();

        assertEquals(List.of("v1"), departed);
        assertEquals(0, snLane.getVehiclesCount());
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, ewLane.getTrafficLight().getColour());
    }

    @Test
    void execute_CompatibleVehicles_BothDepart() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("v2", Direction.NORTH, Direction.SOUTH));

        List<String> departed = newStep().execute();

        assertEquals(2, departed.size());
        assertTrue(departed.containsAll(List.of("v1", "v2")));
        assertEquals(0, snLane.getVehiclesCount());
        assertEquals(0, nsLane.getVehiclesCount());
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.GREEN, nsLane.getTrafficLight().getColour());
    }

    @Test
    void execute_ConflictingVehicles_HigherStayTimeDepartsFirst() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        v1.incrementStayTime();
        snLane.addVehicle(v1);

        ewLane.addVehicle(new Vehicle("v2", Direction.EAST, Direction.WEST));

        List<String> departed = newStep().execute();

        assertEquals(List.of("v1"), departed);
        assertEquals(0, snLane.getVehiclesCount());
        assertEquals(1, ewLane.getVehiclesCount());
        assertEquals(1, ewLane.getVehicles().getFirst().getStayTime());
    }


    @Test
    void execute_PrefersLargerClique() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("v2", Direction.NORTH, Direction.SOUTH));
        ewLane.addVehicle(new Vehicle("v3", Direction.EAST, Direction.WEST));

        List<String> departed = newStep().execute();

        assertEquals(2, departed.size());
        assertTrue(departed.containsAll(List.of("v1", "v2")));
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.GREEN, nsLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, ewLane.getTrafficLight().getColour());
        assertEquals(1, ewLane.getVehicles().getFirst().getStayTime());
    }

    @Test
    void execute_SameSizeCliques_PrefersMoreVehicles() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        snLane.addVehicle(new Vehicle("v2", Direction.SOUTH, Direction.NORTH));
        ewLane.addVehicle(new Vehicle("v3", Direction.EAST, Direction.WEST));

        List<String> departed = newStep().execute();

        assertEquals(List.of("v1"), departed);
        assertEquals(1, snLane.getVehiclesCount());
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, ewLane.getTrafficLight().getColour());
    }

    @Test
    void execute_SameSizeAndVehicles_PrefersHigherStayTime() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        v1.incrementStayTime();
        v1.incrementStayTime();
        snLane.addVehicle(v1);

        ewLane.addVehicle(new Vehicle("v2", Direction.EAST, Direction.WEST));

        List<String> departed = newStep().execute();

        assertEquals(List.of("v1"), departed);
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, ewLane.getTrafficLight().getColour());
        assertEquals(1, ewLane.getVehicles().getFirst().getStayTime());
    }


    @Test
    void execute_VehicleOverMaxStayTime_GetsPriority() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("v2", Direction.NORTH, Direction.SOUTH));

        Vehicle v3 = new Vehicle("v3", Direction.EAST, Direction.WEST);
        for (int i = 0; i < 3; i++) v3.incrementStayTime();
        ewLane.addVehicle(v3);

        List<String> departed = newStep().execute();

        assertEquals(List.of("v3"), departed);
        assertEquals(TrafficLightColour.RED, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, nsLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.GREEN, ewLane.getTrafficLight().getColour());
    }

    @Test
    void execute_MultipleOverMaxStayTime_PicksHighestStayTime() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        for (int i = 0; i < 5; i++) v1.incrementStayTime();
        snLane.addVehicle(v1);

        Vehicle v2 = new Vehicle("v2", Direction.EAST, Direction.WEST);
        for (int i = 0; i < 3; i++) v2.incrementStayTime();
        ewLane.addVehicle(v2);

        List<String> departed = newStep().execute();

        assertEquals(List.of("v1"), departed);
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, ewLane.getTrafficLight().getColour());
    }

    @Test
    void execute_OverMaxStayTime_SameStayTime_TiebrokenByVehicleCount() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        Vehicle v1b = new Vehicle("v1b", Direction.SOUTH, Direction.NORTH);
        for (int i = 0; i < 4; i++) {
            v1.incrementStayTime();
            v1b.incrementStayTime();
        }
        snLane.addVehicle(v1);
        snLane.addVehicle(v1b);

        Vehicle v2 = new Vehicle("v2", Direction.EAST, Direction.WEST);
        for (int i = 0; i < 4; i++) v2.incrementStayTime();
        ewLane.addVehicle(v2);

        List<String> departed = newStep().execute();

        assertTrue(departed.contains("v1"));
        assertEquals(TrafficLightColour.GREEN, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, ewLane.getTrafficLight().getColour());
    }


    @Test
    void execute_MultipleCarsOnLane_OnlyFirstDeparts() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        snLane.addVehicle(new Vehicle("v2", Direction.SOUTH, Direction.NORTH));
        snLane.addVehicle(new Vehicle("v3", Direction.SOUTH, Direction.NORTH));

        List<String> departed = newStep().execute();

        assertEquals(List.of("v1"), departed);
        assertEquals(2, snLane.getVehiclesCount());
        assertEquals("v2", snLane.getVehicles().get(0).getId());
        assertEquals("v3", snLane.getVehicles().get(1).getId());
        assertEquals(0, snLane.getVehicles().get(0).getStayTime());
        assertEquals(0, snLane.getVehicles().get(1).getStayTime());
    }

    @Test
    void execute_NonSelectedLane_AllVehiclesGetStayTimeIncremented() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("v2", Direction.NORTH, Direction.SOUTH));

        Vehicle v3 = new Vehicle("v3", Direction.EAST, Direction.WEST);
        Vehicle v4 = new Vehicle("v4", Direction.EAST, Direction.WEST);
        ewLane.addVehicle(v3);
        ewLane.addVehicle(v4);

        newStep().execute();

        assertEquals(1, v3.getStayTime());
        assertEquals(1, v4.getStayTime());
    }

    @Test
    void execute_SelectedLane_RemainingVehiclesNotIncremented() {
        snLane.addVehicle(new Vehicle("v1", Direction.SOUTH, Direction.NORTH));
        Vehicle v2 = new Vehicle("v2", Direction.SOUTH, Direction.NORTH);
        snLane.addVehicle(v2);

        newStep().execute();

        assertEquals(0, v2.getStayTime());
    }


    @Test
    void execute_TwoSteps_StayTimeAccumulates() {
        Vehicle v1 = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
        v1.incrementStayTime();
        snLane.addVehicle(v1);

        Vehicle v2 = new Vehicle("v2", Direction.EAST, Direction.WEST);
        ewLane.addVehicle(v2);

        List<String> step1 = newStep().execute();
        assertEquals(List.of("v1"), step1);
        assertEquals(1, v2.getStayTime());

        List<String> step2 = newStep().execute();
        assertEquals(List.of("v2"), step2);
    }

    @Test
    void execute_MultipleSteps_MaxStayTimeTriggered() {
        snLane.addVehicle(new Vehicle("sn1", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("ns1", Direction.NORTH, Direction.SOUTH));
        ewLane.addVehicle(new Vehicle("ew1", Direction.EAST, Direction.WEST));

        List<String> step1 = newStep().execute();
        assertTrue(step1.containsAll(List.of("sn1", "ns1")));
        assertEquals(1, ewLane.getVehicles().getFirst().getStayTime());

        snLane.addVehicle(new Vehicle("sn2", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("ns2", Direction.NORTH, Direction.SOUTH));

        List<String> step2 = newStep().execute();
        assertTrue(step2.containsAll(List.of("sn2", "ns2")));
        assertEquals(2, ewLane.getVehicles().getFirst().getStayTime());

        snLane.addVehicle(new Vehicle("sn3", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("ns3", Direction.NORTH, Direction.SOUTH));

        List<String> step3 = newStep().execute();
        assertTrue(step3.containsAll(List.of("sn3", "ns3")));
        assertEquals(3, ewLane.getVehicles().getFirst().getStayTime());

        snLane.addVehicle(new Vehicle("sn4", Direction.SOUTH, Direction.NORTH));
        nsLane.addVehicle(new Vehicle("ns4", Direction.NORTH, Direction.SOUTH));

        List<String> step4 = newStep().execute();
        assertEquals(List.of("ew1"), step4);
        assertEquals(TrafficLightColour.GREEN, ewLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, snLane.getTrafficLight().getColour());
        assertEquals(TrafficLightColour.RED, nsLane.getTrafficLight().getColour());
    }
}
