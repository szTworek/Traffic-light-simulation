package app.engine.command;

import app.model.Direction;
import app.model.Intersection;
import app.model.TrafficLane;
import app.model.Vehicle;

public class AddVehicleCommand implements Command {
    private final String vehicleId;
    private final Direction startRoad;
    private final Direction endRoad;
    private final Intersection intersection;

    public AddVehicleCommand(String vehicleId, Direction startRoad, Direction endRoad, Intersection intersection) {
        this.vehicleId = vehicleId;
        this.startRoad = startRoad;
        this.endRoad = endRoad;
        this.intersection = intersection;
    }

    @Override
    public void execute() {
        Vehicle vehicle = new Vehicle(vehicleId, startRoad, endRoad);
        TrafficLane trafficLane = intersection.getTrafficLane(startRoad, endRoad);
        trafficLane.addVehicle(vehicle);
        intersection.notifyArrival(vehicleId, startRoad, endRoad);
        intersection.notifyObservers();
    }
}
