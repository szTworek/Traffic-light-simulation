package app.engine;

import app.model.Direction;
import app.model.Movement;
import app.model.TrafficLane;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LaneCompatibilityGraphTest {


    private static TrafficLane lane(Direction dir, Movement... movements) {
        return new TrafficLane(dir, new ArrayList<>(List.of(movements)));
    }

    private static LaneCompatibilityGraph buildGraph(TrafficLane... lanes) {
        ArrayList<TrafficLane> laneList = new ArrayList<>(List.of(lanes));
        CompatibilityGraph movementGraph = new CompatibilityGraph(laneList);
        return new LaneCompatibilityGraph(laneList, movementGraph);
    }


    private final TrafficLane snLane = lane(Direction.SOUTH, new Movement(Direction.SOUTH, Direction.NORTH));
    private final TrafficLane nsLane = lane(Direction.NORTH, new Movement(Direction.NORTH, Direction.SOUTH));
    private final TrafficLane ewLane = lane(Direction.EAST, new Movement(Direction.EAST, Direction.WEST));
    private final TrafficLane weLane = lane(Direction.WEST, new Movement(Direction.WEST, Direction.EAST));

    private LaneCompatibilityGraph setupA() {
        return buildGraph(snLane, nsLane, ewLane, weLane);
    }


    private final TrafficLane nStr = lane(Direction.NORTH, new Movement(Direction.NORTH, Direction.SOUTH));
    private final TrafficLane nLeft = lane(Direction.NORTH, new Movement(Direction.NORTH, Direction.EAST));
    private final TrafficLane sStr = lane(Direction.SOUTH, new Movement(Direction.SOUTH, Direction.NORTH));
    private final TrafficLane sLeft = lane(Direction.SOUTH, new Movement(Direction.SOUTH, Direction.WEST));
    private final TrafficLane ewLaneB = lane(Direction.EAST, new Movement(Direction.EAST, Direction.WEST));
    private final TrafficLane weLaneB = lane(Direction.WEST, new Movement(Direction.WEST, Direction.EAST));

    private LaneCompatibilityGraph setupB() {
        return buildGraph(nStr, nLeft, sStr, sLeft, ewLaneB, weLaneB);
    }


    private final TrafficLane nFull = lane(Direction.NORTH,
            new Movement(Direction.NORTH, Direction.SOUTH),
            new Movement(Direction.NORTH, Direction.EAST),
            new Movement(Direction.NORTH, Direction.WEST));
    private final TrafficLane sFull = lane(Direction.SOUTH,
            new Movement(Direction.SOUTH, Direction.NORTH),
            new Movement(Direction.SOUTH, Direction.WEST),
            new Movement(Direction.SOUTH, Direction.EAST));
    private final TrafficLane eFull = lane(Direction.EAST,
            new Movement(Direction.EAST, Direction.WEST),
            new Movement(Direction.EAST, Direction.SOUTH),
            new Movement(Direction.EAST, Direction.NORTH));
    private final TrafficLane wFull = lane(Direction.WEST,
            new Movement(Direction.WEST, Direction.EAST),
            new Movement(Direction.WEST, Direction.NORTH),
            new Movement(Direction.WEST, Direction.SOUTH));

    private LaneCompatibilityGraph setupC() {
        return buildGraph(nFull, sFull, eFull, wFull);
    }


    @Test
    void areCompatible_SameLane_ReturnsTrue() {
        LaneCompatibilityGraph graph = setupA();
        assertTrue(graph.areCompatible(snLane, snLane));
    }

    @Test
    void areCompatible_OppositeStraightLanes_ReturnsTrue() {
        LaneCompatibilityGraph graph = setupA();
        assertTrue(graph.areCompatible(snLane, nsLane));
        assertTrue(graph.areCompatible(nsLane, snLane));
    }

    @Test
    void areCompatible_PerpendicularStraightLanes_ReturnsFalse() {
        LaneCompatibilityGraph graph = setupA();
        assertFalse(graph.areCompatible(snLane, ewLane));
        assertFalse(graph.areCompatible(ewLane, snLane));
    }

    @Test
    void areCompatible_OppositeLeftTurnLanes_ReturnsTrue() {
        LaneCompatibilityGraph graph = setupB();
        assertTrue(graph.areCompatible(nLeft, sLeft));
        assertTrue(graph.areCompatible(sLeft, nLeft));
    }

    @Test
    void areCompatible_StraightVsOppositeLeftTurn_ReturnsFalse() {
        LaneCompatibilityGraph graph = setupB();
        assertFalse(graph.areCompatible(nStr, sLeft));
        assertFalse(graph.areCompatible(sLeft, nStr));
    }

    @Test
    void areCompatible_FullMovementLanes_ReturnsFalse() {
        LaneCompatibilityGraph graph = setupC();
        assertFalse(graph.areCompatible(nFull, sFull));
        assertFalse(graph.areCompatible(eFull, wFull));
        assertFalse(graph.areCompatible(nFull, eFull));
    }


    @Test
    void getCompatibleLanes_StraightLane_ReturnsOppositeLane() {
        LaneCompatibilityGraph graph = setupA();
        assertEquals(Set.of(nsLane), graph.getCompatibleLanes(snLane));
        assertEquals(Set.of(weLane), graph.getCompatibleLanes(ewLane));
    }

    @Test
    void getCompatibleLanes_UnknownLane_ReturnsEmptySet() {
        LaneCompatibilityGraph graph = setupA();
        TrafficLane unknown = lane(Direction.SOUTH, new Movement(Direction.SOUTH, Direction.EAST));
        assertTrue(graph.getCompatibleLanes(unknown).isEmpty());
    }

    @Test
    void getCompatibleLanes_WithTurnLanes_ReturnsCorrectSet() {
        LaneCompatibilityGraph graph = setupB();
        assertEquals(Set.of(sStr, nLeft), graph.getCompatibleLanes(nStr));
        assertEquals(Set.of(nStr, sLeft), graph.getCompatibleLanes(nLeft));
        assertEquals(Set.of(nStr, sLeft), graph.getCompatibleLanes(sStr));
        assertEquals(Set.of(sStr, nLeft), graph.getCompatibleLanes(sLeft));
        assertEquals(Set.of(weLaneB), graph.getCompatibleLanes(ewLaneB));
    }


    @Test
    void findMaximalCliques_FourStraightLanes_ReturnsTwoCliques() {
        LaneCompatibilityGraph graph = setupA();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliques(
                List.of(snLane, nsLane, ewLane, weLane));

        assertEquals(2, cliques.size());
        assertTrue(cliques.contains(Set.of(snLane, nsLane)));
        assertTrue(cliques.contains(Set.of(ewLane, weLane)));
    }

    @Test
    void findMaximalCliques_EmptyCandidates_ReturnsSingleEmptyClique() {
        LaneCompatibilityGraph graph = setupA();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliques(List.of());

        assertEquals(1, cliques.size());
        assertTrue(cliques.getFirst().isEmpty());
    }

    @Test
    void findMaximalCliques_SingleCandidate_ReturnsSingletonClique() {
        LaneCompatibilityGraph graph = setupA();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliques(List.of(snLane));

        assertEquals(1, cliques.size());
        assertEquals(Set.of(snLane), cliques.getFirst());
    }

    @Test
    void findMaximalCliques_IncompatibleSubset_ReturnsSingletonCliques() {
        LaneCompatibilityGraph graph = setupA();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliques(List.of(snLane, ewLane));

        assertEquals(2, cliques.size());
        assertTrue(cliques.contains(Set.of(snLane)));
        assertTrue(cliques.contains(Set.of(ewLane)));
    }

    @Test
    void findMaximalCliques_FullMovementLanes_ReturnsOnlySingletonCliques() {
        LaneCompatibilityGraph graph = setupC();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliques(
                List.of(nFull, sFull, eFull, wFull));

        assertEquals(4, cliques.size());
        for (Set<TrafficLane> clique : cliques) {
            assertEquals(1, clique.size());
        }
        assertTrue(cliques.contains(Set.of(nFull)));
        assertTrue(cliques.contains(Set.of(sFull)));
        assertTrue(cliques.contains(Set.of(eFull)));
        assertTrue(cliques.contains(Set.of(wFull)));
    }

    @Test
    void findMaximalCliques_WithTurnLanes_ReturnsFiveCliques() {
        LaneCompatibilityGraph graph = setupB();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliques(
                List.of(nStr, nLeft, sStr, sLeft, ewLaneB, weLaneB));

        assertEquals(5, cliques.size());
        assertTrue(cliques.contains(Set.of(nStr, sStr)));
        assertTrue(cliques.contains(Set.of(nStr, nLeft)));
        assertTrue(cliques.contains(Set.of(sStr, sLeft)));
        assertTrue(cliques.contains(Set.of(nLeft, sLeft)));
        assertTrue(cliques.contains(Set.of(ewLaneB, weLaneB)));
    }


    @Test
    void findMaximalCliquesContaining_RequiredLaneNotInCandidates_ReturnsEmptyList() {
        LaneCompatibilityGraph graph = setupA();
        TrafficLane absent = lane(Direction.SOUTH, new Movement(Direction.SOUTH, Direction.EAST));

        List<Set<TrafficLane>> cliques = graph.findMaximalCliquesContaining(
                absent, List.of(snLane, nsLane));

        assertTrue(cliques.isEmpty());
    }

    @Test
    void findMaximalCliquesContaining_RequiredLaneInCandidates_ReturnsCliquesWithThatLane() {
        LaneCompatibilityGraph graph = setupA();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliquesContaining(
                snLane, List.of(snLane, nsLane, ewLane, weLane));

        assertEquals(1, cliques.size());
        assertEquals(Set.of(snLane, nsLane), cliques.getFirst());
    }

    @Test
    void findMaximalCliquesContaining_NoCompatibleInCandidates_ReturnsSingleton() {
        LaneCompatibilityGraph graph = setupA();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliquesContaining(
                snLane, List.of(snLane, ewLane));

        assertEquals(1, cliques.size());
        assertEquals(Set.of(snLane), cliques.getFirst());
    }

    @Test
    void findMaximalCliquesContaining_WithTurnLanes_ReturnsCliquesContainingRequired() {
        LaneCompatibilityGraph graph = setupB();
        List<Set<TrafficLane>> cliques = graph.findMaximalCliquesContaining(
                nLeft, List.of(nStr, nLeft, sStr, sLeft, ewLaneB, weLaneB));

        assertEquals(2, cliques.size());
        assertTrue(cliques.contains(Set.of(nStr, nLeft)));
        assertTrue(cliques.contains(Set.of(nLeft, sLeft)));
    }
}
