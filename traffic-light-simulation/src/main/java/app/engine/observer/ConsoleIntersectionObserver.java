package app.engine.observer;

import app.model.Direction;
import app.model.Movement;
import app.model.TrafficLightColour;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleIntersectionObserver implements IntersectionObserver {

    private static final String SEPARATOR = "=======================================================";
    private static final String ANSI_RESET = "\033[0m";
    private static final String ANSI_RED = "\033[31m";
    private static final String ANSI_GREEN = "\033[32m";
    private static final String ANSI_YELLOW = "\033[33m";

    @Override
    public void onIntersectionChanged(IntersectionSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("INTERSECTION STATUS\n");
        sb.append(SEPARATOR).append("\n");

        Map<Direction, List<LaneSnapshot>> grouped = new LinkedHashMap<>();
        for (LaneSnapshot lane : snapshot.lanes()) {
            grouped.computeIfAbsent(lane.direction(), k -> new ArrayList<>()).add(lane);
        }

        for (Map.Entry<Direction, List<LaneSnapshot>> entry : grouped.entrySet()) {
            sb.append("\n[FROM ").append(entry.getKey()).append("]\n");

            for (LaneSnapshot lane : entry.getValue()) {
                String moves = lane.possibleMovements().stream()
                        .map(Movement::to)
                        .map(Direction::name)
                        .collect(Collectors.joining(", "));

                String lightText = formatLight(lane.lightColour());

                String vehiclesText;
                if (lane.vehicles().isEmpty()) {
                    vehiclesText = "-";
                } else {
                    vehiclesText = lane.vehicles().stream()
                            .map(v -> "[" + v.id() + ", stay time: " + v.stayTime() + "]")
                            .collect(Collectors.joining(", "));
                }

                sb.append(String.format("  | Moves: %-18s | Light: %s | Vehicles Number: %d | Vehicles: %s%n",
                        moves, lightText, lane.vehicleCount(), vehiclesText));
            }
        }

        sb.append("\n").append(SEPARATOR).append("\n");
        System.out.print(sb);
    }

    private String formatLight(TrafficLightColour colour) {
        return switch (colour) {
            case RED -> ANSI_RED + "RED" + ANSI_RESET;
            case GREEN -> ANSI_GREEN + "GREEN" + ANSI_RESET;
            case YELLOW -> ANSI_YELLOW + "YELLOW" + ANSI_RESET;
        };
    }
}
