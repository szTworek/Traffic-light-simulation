package app.engine;

import app.model.Movement;
import app.model.TrafficLane;

import java.util.*;

public class LaneCompatibilityGraph {
    private final Map<TrafficLane, Set<TrafficLane>> adjacency;

    public LaneCompatibilityGraph(ArrayList<TrafficLane> trafficLanes, CompatibilityGraph movementGraph) {
        adjacency = new HashMap<>();

        for (TrafficLane lane : trafficLanes) {
            adjacency.put(lane, new HashSet<>());
        }

        for (int i = 0; i < trafficLanes.size(); i++) {
            for (int j = i + 1; j < trafficLanes.size(); j++) {
                TrafficLane a = trafficLanes.get(i);
                TrafficLane b = trafficLanes.get(j);
                if (lanesCompatible(a, b, movementGraph)) {
                    adjacency.get(a).add(b);
                    adjacency.get(b).add(a);
                }
            }
        }
    }

    public Set<TrafficLane> getCompatibleLanes(TrafficLane lane) {
        return adjacency.getOrDefault(lane, Collections.emptySet());
    }

    public boolean areCompatible(TrafficLane a, TrafficLane b) {
        if (a == b) return true;
        return adjacency.containsKey(a) && adjacency.get(a).contains(b);
    }

    public List<Set<TrafficLane>> findMaximalCliques(List<TrafficLane> candidates) {
        Set<TrafficLane> candidateSet = new HashSet<>(candidates);
        List<Set<TrafficLane>> cliques = new ArrayList<>();
        bronKerbosch(new HashSet<>(), new HashSet<>(candidateSet), new HashSet<>(), candidateSet, cliques);
        return cliques;
    }

    public List<Set<TrafficLane>> findMaximalCliquesContaining(TrafficLane requiredLane, List<TrafficLane> candidates) {
        if (!candidates.contains(requiredLane)) {
            return Collections.emptyList();
        }

        Set<TrafficLane> candidateSet = new HashSet<>(candidates);
        List<Set<TrafficLane>> cliques = new ArrayList<>();

        Set<TrafficLane> R = new HashSet<>();
        R.add(requiredLane);

        Set<TrafficLane> P = new HashSet<>(getCompatibleLanes(requiredLane));
        P.retainAll(candidateSet);

        Set<TrafficLane> X = new HashSet<>();

        bronKerbosch(R, P, X, candidateSet, cliques);

        return cliques;
    }

    private void bronKerbosch(Set<TrafficLane> R, Set<TrafficLane> P, Set<TrafficLane> X,
                               Set<TrafficLane> scope, List<Set<TrafficLane>> cliques) {
        if (P.isEmpty() && X.isEmpty()) {
            cliques.add(new HashSet<>(R));
            return;
        }

        TrafficLane pivot = pickPivot(P, X, scope);
        Set<TrafficLane> pivotNeighbors = neighborsInScope(pivot, scope);

        for (TrafficLane v : new HashSet<>(P)) {
            if (pivotNeighbors.contains(v)) continue;

            R.add(v);
            Set<TrafficLane> neighborsV = neighborsInScope(v, scope);
            Set<TrafficLane> newP = intersect(P, neighborsV);
            Set<TrafficLane> newX = intersect(X, neighborsV);
            bronKerbosch(R, newP, newX, scope, cliques);
            R.remove(v);
            P.remove(v);
            X.add(v);
        }
    }

    private TrafficLane pickPivot(Set<TrafficLane> P, Set<TrafficLane> X, Set<TrafficLane> scope) {
        TrafficLane best = null;
        int bestCount = -1;
        Set<TrafficLane> union = new HashSet<>(P);
        union.addAll(X);
        for (TrafficLane lane : union) {
            int count = intersectCount(P, neighborsInScope(lane, scope));
            if (count > bestCount) {
                bestCount = count;
                best = lane;
            }
        }
        return best;
    }

    private Set<TrafficLane> neighborsInScope(TrafficLane lane, Set<TrafficLane> scope) {
        Set<TrafficLane> result = new HashSet<>(getCompatibleLanes(lane));
        result.retainAll(scope);
        return result;
    }

    private static Set<TrafficLane> intersect(Set<TrafficLane> a, Set<TrafficLane> b) {
        Set<TrafficLane> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }

    private static int intersectCount(Set<TrafficLane> a, Set<TrafficLane> b) {
        int count = 0;
        for (TrafficLane lane : a) {
            if (b.contains(lane)) count++;
        }
        return count;
    }

    private boolean lanesCompatible(TrafficLane a, TrafficLane b, CompatibilityGraph movementGraph) {
        for (Movement movA : a.getPossibleMovements()) {
            for (Movement movB : b.getPossibleMovements()) {
                if (!movementGraph.areCompatible(movA, movB)) {
                    return false;
                }
            }
        }
        return true;
    }
}
