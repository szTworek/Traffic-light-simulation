package app.engine.observer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VehicleStatusObserver implements IntersectionObserver {

    private static final String ANSI_RESET = "\033[0m";
    private static final String ANSI_MAGENTA = "\033[35m";

    @Override
    public void onIntersectionChanged(IntersectionSnapshot snapshot) {
        List<VehicleSnapshot> allVehicles = new ArrayList<>();
        for (LaneSnapshot lane : snapshot.lanes()) {
            allVehicles.addAll(lane.vehicles());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ANSI_MAGENTA);

        if (allVehicles.isEmpty()) {
            sb.append("--- No vehicles on intersection ---");
        } else {
            allVehicles.sort(Comparator.comparingInt(VehicleSnapshot::stayTime).reversed());
            sb.append("--- Vehicles on intersection (").append(allVehicles.size()).append(") ---\n");
            for (VehicleSnapshot v : allVehicles) {
                sb.append(String.format("  stayTime: %d | %s | %s -> %s%n",
                        v.stayTime(), v.id(), v.startRoad(), v.endRoad()));
            }
            sb.append("---");
        }

        sb.append(ANSI_RESET);
        System.out.println(sb);
    }
}
