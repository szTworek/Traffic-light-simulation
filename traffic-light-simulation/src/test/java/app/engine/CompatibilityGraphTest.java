package app.engine;

import app.model.Direction;
import app.model.Movement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CompatibilityGraphTest {

    @Test
    void doConflict_SameStartDifferentEnd_Conflicts() {
        Movement m1 = new Movement(Direction.SOUTH, Direction.NORTH);
        Movement m2 = new Movement(Direction.SOUTH, Direction.WEST);

        assertFalse(CompatibilityGraph.doConflict(m1, m2));
    }

    @Test
    void doConflict_SameEndDifferentStart_Conflicts() {
        Movement m1 = new Movement(Direction.SOUTH, Direction.NORTH);
        Movement m2 = new Movement(Direction.EAST, Direction.NORTH);
        
        assertTrue(CompatibilityGraph.doConflict(m1, m2));
    }

    @Test
    void doConflict_OppositeDirections_NoConflict() {
        Movement m1 = new Movement(Direction.SOUTH, Direction.NORTH);
        Movement m2 = new Movement(Direction.NORTH, Direction.SOUTH);
        
        assertFalse(CompatibilityGraph.doConflict(m1, m2));
    }

    @Test
    void doConflict_IntersectingStraightPaths_Conflicts() {
        Movement m1 = new Movement(Direction.SOUTH, Direction.NORTH);
        Movement m2 = new Movement(Direction.EAST, Direction.WEST);
        
        assertTrue(CompatibilityGraph.doConflict(m1, m2));
    }
    
    @Test
    void doConflict_LeftTurnCrossingStraight_Conflicts() {
        Movement straight = new Movement(Direction.SOUTH, Direction.NORTH);
        Movement leftTurn = new Movement(Direction.NORTH, Direction.EAST);
        
        assertTrue(CompatibilityGraph.doConflict(straight, leftTurn));
    }

    @Test
    void doConflict_OppositeLeftTurns_NoConflict() {
        Movement leftTurn1 = new Movement(Direction.SOUTH, Direction.WEST);
        Movement leftTurn2 = new Movement(Direction.NORTH, Direction.EAST);

        assertFalse(CompatibilityGraph.doConflict(leftTurn1, leftTurn2));
    }

    @Test
    void constructor_CreatesComprehensiveCompatibilityGraph() {
        Movement n_s = new Movement(Direction.NORTH, Direction.SOUTH);
        Movement n_e = new Movement(Direction.NORTH, Direction.EAST);
        Movement n_w = new Movement(Direction.NORTH, Direction.WEST);

        Movement s_n = new Movement(Direction.SOUTH, Direction.NORTH);
        Movement s_w = new Movement(Direction.SOUTH, Direction.WEST);
        Movement s_e = new Movement(Direction.SOUTH, Direction.EAST);

        Movement e_w = new Movement(Direction.EAST, Direction.WEST);
        Movement e_s = new Movement(Direction.EAST, Direction.SOUTH);
        Movement e_n = new Movement(Direction.EAST, Direction.NORTH);

        Movement w_e = new Movement(Direction.WEST, Direction.EAST);
        Movement w_n = new Movement(Direction.WEST, Direction.NORTH);
        Movement w_s = new Movement(Direction.WEST, Direction.SOUTH);

        ArrayList<Movement> allMovements = new ArrayList<>(java.util.List.of(
                n_s, n_e, n_w, s_n, s_w, s_e, e_w, e_s, e_n, w_e, w_n, w_s
        ));

        app.model.TrafficLane laneN = new app.model.TrafficLane(Direction.NORTH, new ArrayList<>(java.util.List.of(n_s, n_e, n_w)));
        app.model.TrafficLane laneS = new app.model.TrafficLane(Direction.SOUTH, new ArrayList<>(java.util.List.of(s_n, s_w, s_e)));
        app.model.TrafficLane laneE = new app.model.TrafficLane(Direction.EAST, new ArrayList<>(java.util.List.of(e_w, e_s, e_n)));
        app.model.TrafficLane laneW = new app.model.TrafficLane(Direction.WEST, new ArrayList<>(java.util.List.of(w_e, w_n, w_s)));

        ArrayList<app.model.TrafficLane> lanes = new ArrayList<>(java.util.List.of(laneN, laneS, laneE, laneW));

        CompatibilityGraph graph = new CompatibilityGraph(lanes);

        for (Movement m1 : allMovements) {
            java.util.Set<Movement> actualCompatible = graph.getCompatibleMovements(m1);
            
            java.util.Set<Movement> expectedCompatible = new java.util.HashSet<>();
            for (Movement m2 : allMovements) {
                if (!m1.equals(m2) && (!CompatibilityGraph.doConflict(m1, m2) && !CompatibilityGraph.doConflict(m2, m1))) {
                    expectedCompatible.add(m2);
                }
            }

            assertEquals(expectedCompatible.size(), actualCompatible.size(),
                    "Size mismatch for compatible movements of: " + m1.from() + "->" + m1.to());
            assertTrue(actualCompatible.containsAll(expectedCompatible),
                    "Content mismatch for compatible movements of: " + m1.from() + "->" + m1.to());
        }

        java.util.Set<Movement> nsCompatible = graph.getCompatibleMovements(n_e);
        assertTrue(nsCompatible.contains(s_w), "Opposite left turns should be compatible (N->E and S->W)");
        assertFalse(nsCompatible.contains(e_w), "Left turn and crossing straight should conflict");
    }
}
