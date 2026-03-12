package app.model;

import java.util.Objects;

public class Vehicle {
    private final Direction startRoad;
    private final Direction endRoad;
    private final String id;
    private int stayTime = 0;

    public Vehicle(String id, Direction startRoad, Direction endRoad) {
        Objects.requireNonNull(id, "vehicle id must not be null");
        Objects.requireNonNull(startRoad, "startRoad must not be null");
        Objects.requireNonNull(endRoad, "endRoad must not be null");
        if (startRoad == endRoad) {
            throw new IllegalArgumentException("startRoad and endRoad must differ, got: " + startRoad);
        }
        this.id = id;
        this.startRoad = startRoad;
        this.endRoad = endRoad;
    }

    public Direction getStartRoad() {
        return startRoad;
    }

    public Direction getEndRoad() {
        return endRoad;
    }

    public String getId() {
        return id;
    }

    public Movement getMovement() {
        return new Movement(startRoad, endRoad);
    }

    public int getStayTime() {
        return stayTime;
    }

    public void incrementStayTime() {
        stayTime++;
    }
}
