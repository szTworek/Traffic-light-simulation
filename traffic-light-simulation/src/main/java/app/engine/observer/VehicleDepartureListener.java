package app.engine.observer;

import app.model.Direction;

@FunctionalInterface
public interface VehicleDepartureListener {
    void onVehicleDeparted(String vehicleId, Direction from, Direction to);
}
