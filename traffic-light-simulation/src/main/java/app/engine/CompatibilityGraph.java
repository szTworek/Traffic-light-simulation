package app.engine;

import app.model.Movement;
import app.model.MovementType;
import app.model.TrafficLane;

import java.util.*;

public class CompatibilityGraph {
    private final Map<Movement, Set<Movement>> adjacency;

    public CompatibilityGraph(ArrayList<TrafficLane> trafficLanes) {
        adjacency = new HashMap<>();

        Set<Movement> allMovements = new LinkedHashSet<>();
        for (TrafficLane lane : trafficLanes) {
            allMovements.addAll(lane.getPossibleMovements());
        }

        for (Movement m : allMovements) {
            adjacency.put(m, new HashSet<>());
        }

        List<Movement> movementList = new ArrayList<>(allMovements);
        for (int i = 0; i < movementList.size(); i++) {
            for (int j = i + 1; j < movementList.size(); j++) {
                Movement a = movementList.get(i);
                Movement b = movementList.get(j);
                if (!doConflict(a, b)) {
                    adjacency.get(a).add(b);
                    adjacency.get(b).add(a);
                }
            }
        }
    }

    public Set<Movement> getCompatibleMovements(Movement m) {
        return adjacency.getOrDefault(m, Collections.emptySet());
    }

    public boolean areCompatible(Movement a, Movement b) {
        if (a.equals(b)) return true;
        return adjacency.containsKey(a) && adjacency.get(a).contains(b);
    }

    static boolean doConflict(Movement a, Movement b) {
        if (a.from() == b.from()) return false;
        if (a.to() == b.to()) return true;

        MovementType typeA = a.getType();
        MovementType typeB = b.getType();

        if ((typeA == MovementType.STRAIGHT && typeB == MovementType.LEFT) ||
                (typeA == MovementType.LEFT && typeB == MovementType.STRAIGHT)) {
            return true;
        }

        if (typeA == MovementType.STRAIGHT && typeB == MovementType.STRAIGHT) {
            return a.from() != b.to();
        }

        if (typeA == MovementType.LEFT && typeB == MovementType.LEFT) {
            return a.from() != b.from().getOpposite();
        }

        return false;
    }
}
