package app.engine;

import app.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Step {

    private final Intersection intersection;

    public Step(Intersection intersection) {
        this.intersection = intersection;
    }

    public List<String> execute(){
        ArrayList<TrafficLane> trafficLanes = intersection.getTrafficLanes();
        List<TrafficLane> activeLanes = trafficLanes.stream()
                .filter(lane -> lane.getVehiclesCount() != 0)
                .toList();

        if (activeLanes.isEmpty()) {
            for (TrafficLane lane : trafficLanes) {
                lane.getTrafficLight().setColour(TrafficLightColour.RED);
            }
            intersection.notifyObservers();
            return Collections.emptyList();
        }

        Map<TrafficLane, Integer> overMaxStayTimeLanes = activeLanes.stream()
                .filter(lane -> lane.getStayTime() >= intersection.getMaxStayTime())
                .collect(Collectors.toMap(
                        Function.identity(),
                        TrafficLane::getStayTime
                ));

        Set<TrafficLane> selectedLanes = new HashSet<>();

        if (!overMaxStayTimeLanes.isEmpty()) {
            int maxTime = Collections.max(overMaxStayTimeLanes.values());

            List<TrafficLane> maxTimeLanes = overMaxStayTimeLanes.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxTime)
                    .map(Map.Entry::getKey)
                    .toList();

            TrafficLane selectedOverMaxStayTimeLane = maxTimeLanes.getFirst();
            for (TrafficLane lane : maxTimeLanes) {
                if (lane.getVehiclesCount() > selectedOverMaxStayTimeLane.getVehiclesCount()
                        || (lane.getVehiclesCount() == selectedOverMaxStayTimeLane.getVehiclesCount()
                            && lane.getTotalStayTime() > selectedOverMaxStayTimeLane.getTotalStayTime())) {
                    selectedOverMaxStayTimeLane = lane;
                }
            }

            List<Set<TrafficLane>> maximalCliques = intersection.getLaneCompatibilityGraph().findMaximalCliquesContaining(selectedOverMaxStayTimeLane, activeLanes);
            selectedLanes = selectBestClique(maximalCliques);
        }
        else {
            List<Set<TrafficLane>> maximalCliques = intersection.getLaneCompatibilityGraph().findMaximalCliques(activeLanes);

            selectedLanes = selectBestClique(maximalCliques);
        }

        Map<TrafficLane, TrafficLightColour> targetColours = new HashMap<>();
        for (TrafficLane lane : trafficLanes) {
            targetColours.put(lane, selectedLanes.contains(lane) ? TrafficLightColour.GREEN : TrafficLightColour.RED);
        }

        boolean anyChange = false;
        for (TrafficLane lane : trafficLanes) {
            TrafficLightColour current = lane.getTrafficLight().getColour();
            TrafficLightColour target = targetColours.get(lane);
            if (current != target) {
                lane.getTrafficLight().setColour(TrafficLightColour.YELLOW);
                anyChange = true;
            }
        }

        if (anyChange) {
            intersection.notifyObservers();
        }

        for (TrafficLane lane : trafficLanes) {
            lane.getTrafficLight().setColour(targetColours.get(lane));
        }
        intersection.notifyObservers();

        List<String> departedVehicleIds = new ArrayList<>();
        for (TrafficLane lane : trafficLanes) {
            if (selectedLanes.contains(lane)) {
                Vehicle departed = lane.removeFirstVehicle();
                departedVehicleIds.add(departed.getId());
                intersection.notifyDeparture(departed.getId(), departed.getStartRoad(), departed.getEndRoad());
            } else {
                for (Vehicle vehicle : lane.getVehicles()) {
                    vehicle.incrementStayTime();
                }
            }
        }
        intersection.notifyObservers();

        return departedVehicleIds;
    }

    private Set<TrafficLane> selectBestClique(List<Set<TrafficLane>> cliques) {
        if (cliques.isEmpty()) return Collections.emptySet();

        Set<TrafficLane> best = null;
        int bestSize = -1;
        int bestVehicles = -1;
        int bestStayTime = -1;

        for (Set<TrafficLane> clique : cliques) {
            int size = clique.size();
            int vehicles = 0;
            int stayTime = 0;
            for (TrafficLane lane : clique) {
                vehicles += lane.getVehiclesCount();
                stayTime += lane.getTotalStayTime();
            }
            if (size > bestSize
                    || (size == bestSize && vehicles > bestVehicles)
                    || (size == bestSize && vehicles == bestVehicles && stayTime > bestStayTime)) {
                best = clique;
                bestSize = size;
                bestVehicles = vehicles;
                bestStayTime = stayTime;
            }
        }

        return best;
    }
}
