package app.engine.observer;

import app.model.Direction;

public record VehicleSnapshot(String id, Direction startRoad, Direction endRoad, int stayTime) {}
