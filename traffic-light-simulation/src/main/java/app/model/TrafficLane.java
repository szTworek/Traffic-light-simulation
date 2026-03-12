package app.model;

import java.util.ArrayList;

public class TrafficLane {
    private ArrayList<Vehicle> vehicles;
    private final Direction direction;
    private final ArrayList<Movement> possibleMovements;
    private final TrafficLight trafficLight;

    public TrafficLane(Direction direction, ArrayList<Movement> possibleMovements) {
        this.direction = direction;
        this.trafficLight = new TrafficLight(TrafficLightColour.RED);
        this.vehicles = new ArrayList<>();
        this.possibleMovements = possibleMovements;
    }

    public Direction getDirection() {
        return direction;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public Vehicle removeFirstVehicle() {
        if (vehicles.isEmpty()) {
            throw new IllegalStateException("Cannot remove vehicle from empty lane: " + direction);
        }
        return vehicles.removeFirst();
    }

    public ArrayList<Movement> getPossibleMovements() {
        return possibleMovements;
    }
    public int getVehiclesCount() {
        return vehicles.size();
    }

    public int getStayTime(){
        if (vehicles.isEmpty()) return 0;
        else{
            return vehicles.getFirst().getStayTime();
        }
    }

    public int getTotalStayTime() {
        int totalStayTime = 0;
        for (Vehicle vehicle : vehicles) {
            totalStayTime += vehicle.getStayTime();
        }
        return totalStayTime;
    }

}
