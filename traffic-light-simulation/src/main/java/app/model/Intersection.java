package app.model;

import app.engine.CompatibilityGraph;
import app.engine.LaneCompatibilityGraph;
import app.engine.observer.IntersectionObserver;
import app.engine.observer.IntersectionSnapshot;
import app.engine.observer.LaneSnapshot;
import app.engine.observer.VehicleArrivalListener;
import app.engine.observer.VehicleDepartureListener;
import app.engine.observer.VehicleSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Intersection {
    private final ArrayList<TrafficLane> trafficLanes;
    private final LaneCompatibilityGraph laneCompatibilityGraph;
    private final int maxStayTime;
    private final boolean fastSimulation;
    private final List<IntersectionObserver> observers = new ArrayList<>();
    private final List<VehicleDepartureListener> departureListeners = new ArrayList<>();
    private final List<VehicleArrivalListener> arrivalListeners = new ArrayList<>();

    public Intersection(ArrayList<TrafficLane> trafficLanes, int maxStayTime, boolean fastSimulation) {
        this.trafficLanes = trafficLanes;
        this.maxStayTime = maxStayTime;
        this.fastSimulation = fastSimulation;
        CompatibilityGraph compatibilityGraph = new CompatibilityGraph(trafficLanes);
        this.laneCompatibilityGraph = new LaneCompatibilityGraph(trafficLanes, compatibilityGraph);
    }

    public void addObserver(IntersectionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IntersectionObserver observer) {
        observers.remove(observer);
    }

    public void addDepartureListener(VehicleDepartureListener listener) {
        departureListeners.add(listener);
    }

    public void addArrivalListener(VehicleArrivalListener listener) {
        arrivalListeners.add(listener);
    }

    public void notifyArrival(String vehicleId, Direction from, Direction to) {
        for (VehicleArrivalListener listener : arrivalListeners) {
            listener.onVehicleArrived(vehicleId, from, to);
        }
    }

    public void notifyDeparture(String vehicleId, Direction from, Direction to) {
        for (VehicleDepartureListener listener : departureListeners) {
            listener.onVehicleDeparted(vehicleId, from, to);
        }
    }

    public void notifyObservers() {
        IntersectionSnapshot snapshot = createSnapshot();
        for (IntersectionObserver observer : observers) {
            observer.onIntersectionChanged(snapshot);
        }
        
        if (!fastSimulation) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private IntersectionSnapshot createSnapshot() {
        List<LaneSnapshot> laneSnapshots = new ArrayList<>();
        for (TrafficLane lane : trafficLanes) {
            List<VehicleSnapshot> vehicleSnapshots = lane.getVehicles().stream()
                    .map(v -> new VehicleSnapshot(v.getId(), v.getStartRoad(), v.getEndRoad(), v.getStayTime()))
                    .toList();

            laneSnapshots.add(new LaneSnapshot(
                    lane.getDirection(),
                    List.copyOf(lane.getPossibleMovements()),
                    lane.getTrafficLight().getColour(),
                    lane.getVehiclesCount(),
                    vehicleSnapshots
            ));
        }
        return new IntersectionSnapshot(laneSnapshots);
    }

    public ArrayList<TrafficLane> getTrafficLanes() {
        return trafficLanes;
    }

    public TrafficLane getTrafficLane(Direction startRoad, Direction endRoad) {
        Movement movement = new Movement(startRoad, endRoad);

        for (TrafficLane trafficLane : trafficLanes) {
            if (trafficLane.getPossibleMovements().contains(movement)) {
                return trafficLane;
            }
        }
        throw new IllegalStateException("No traffic lane found for movement: " + startRoad + " -> " + endRoad);
    }

    public LaneCompatibilityGraph getLaneCompatibilityGraph() {
        return laneCompatibilityGraph;
    }

    public int getMaxStayTime() {
        return maxStayTime;
    }
}
