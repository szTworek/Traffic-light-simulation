package app.engine.observer;

import app.model.Direction;
import app.model.Movement;
import app.model.TrafficLightColour;

import java.util.List;

public record LaneSnapshot(
        Direction direction,
        List<Movement> possibleMovements,
        TrafficLightColour lightColour,
        int vehicleCount,
        List<VehicleSnapshot> vehicles
) {}
