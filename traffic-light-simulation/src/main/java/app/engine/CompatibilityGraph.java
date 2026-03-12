package app.engine;

import app.model.Movement;
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

        if (a.from() == b.to() && a.to() == b.from()) return false;

        if (isLeftTurn(a) && isLeftTurn(b) && a.from() == b.from().getOpposite()) {
            return false;
        }

        boolean sharedVertex = a.from() == b.to() || a.to() == b.from() || a.to() == b.to();
        if (!sharedVertex) {
            return chordsIntersect(
                    a.from().toIndex(), a.to().toIndex(),
                    b.from().toIndex(), b.to().toIndex()
            );
        }

        return isLeftTurn(a) || isLeftTurn(b);
    }

    private static boolean isLeftTurn(Movement m) {
        return m.to() == m.from().getLeft();
    }

    private static boolean chordsIntersect(int p, int q, int r, int s) {
        return isBetween(p, q, r) != isBetween(p, q, s);
    }

    private static boolean isBetween(int p, int q, int x) {
        int n = 4;
        p = ((p % n) + n) % n;
        q = ((q % n) + n) % n;
        x = ((x % n) + n) % n;
        if (p < q) return p < x && x < q;
        return p < x || x < q;
    }
}
