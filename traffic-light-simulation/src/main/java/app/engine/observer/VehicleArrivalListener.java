package app.engine.observer;

import app.model.Direction;

@FunctionalInterface
public interface VehicleArrivalListener {
    void onVehicleArrived(String vehicleId, Direction from, Direction to);
}
