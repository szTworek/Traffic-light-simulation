package app.engine.observer;

import app.model.Direction;
import app.model.Movement;
import app.model.TrafficLightColour;

import java.util.*;
import java.util.stream.Collectors;

public class AsciiIntersectionRenderer {
    private static final int LW = 7;
    private static final int VRH = 5;
    private static final int HRW = 22;

    private static final String ANSI_RESET = "\033[0m";
    private static final String ANSI_RED = "\033[31m";
    private static final String ANSI_GREEN = "\033[32m";
    private static final String ANSI_YELLOW = "\033[33m";

    public String render(IntersectionSnapshot snapshot) {
        Map<Direction, List<LaneSnapshot>> byDir = new LinkedHashMap<>();
        for (Direction d : Direction.values()) byDir.put(d, new ArrayList<>());
        for (LaneSnapshot l : snapshot.lanes()) byDir.get(l.direction()).add(l);

        List<LaneSnapshot> nLanes = sortLanesByPosition(byDir.get(Direction.NORTH), Direction.NORTH);
        List<LaneSnapshot> sLanes = sortLanesByPosition(byDir.get(Direction.SOUTH), Direction.SOUTH);
        List<LaneSnapshot> eLanes = sortLanesByPosition(byDir.get(Direction.EAST), Direction.EAST);
        List<LaneSnapshot> wLanes = sortLanesByPosition(byDir.get(Direction.WEST), Direction.WEST);

        int nIn = nLanes.size(), sIn = sLanes.size();
        int eIn = eLanes.size(), wIn = wLanes.size();

        int leftCols = Math.max(nIn, 1);
        int rightCols = Math.max(1, sIn);
        int vertW = 2 + (leftCols + rightCols) * (LW + 1);

        int horizRows = Math.max(wIn + 2, eIn + 2);

        int gridW = HRW + vertW + HRW;
        int gridH = VRH + 1 + horizRows + 1 + VRH;

        CharGrid grid = new CharGrid(gridW, gridH);

        int cLeft = HRW;
        int cRight = HRW + vertW - 1;
        int cTop = VRH;
        int cBot = VRH + 1 + horizRows;

        List<Integer> northSeps = roadSepPositions(nIn, 1, leftCols);
        List<Integer> southSeps = roadSepPositions(1, sIn, leftCols);

        drawVertSeparators(grid, cLeft, 0, VRH, northSeps);
        drawNorthContent(grid, nLanes, cLeft, leftCols, vertW);

        drawHBorder(grid, cTop, gridW, cLeft, northSeps);
        drawHBorder(grid, cBot, gridW, cLeft, southSeps);

        drawCenterSection(grid, wLanes, eLanes, cTop + 1, cLeft, cRight, horizRows, gridW);

        drawVertSeparators(grid, cLeft, cBot + 1, VRH, southSeps);
        drawSouthContent(grid, sLanes, cLeft, cBot + 1, leftCols, vertW);

        return grid.toString();
    }

    // --- Vertical road (N/S) ---

    private void drawVertSeparators(CharGrid grid, int cLeft, int startRow, int rows,
                                     List<Integer> seps) {
        for (int pos : seps) {
            grid.vLine(startRow, cLeft + pos, rows, '|');
        }
    }

    private List<Integer> roadSepPositions(int activeLeft, int activeRight, int leftCols) {
        List<Integer> p = new ArrayList<>();
        p.add(0);
        for (int k = 1; k < activeLeft; k++) {
            p.add(k * (LW + 1));
        }
        int dblSep = leftCols * (LW + 1);
        p.add(dblSep);
        p.add(dblSep + 1);
        for (int k = 1; k < activeRight; k++) {
            p.add(dblSep + 1 + k * (LW + 1));
        }
        p.add(dblSep + 1 + activeRight * (LW + 1));
        return p;
    }

    private int leftLaneCol(int cLeft, int k) {
        return cLeft + 1 + k * (LW + 1);
    }

    private int rightLaneCol(int cLeft, int leftCols, int k) {
        return cLeft + leftCols * (LW + 1) + 2 + k * (LW + 1);
    }

    private void drawNorthContent(CharGrid grid, List<LaneSnapshot> nLanes,
                                   int cLeft, int leftCols, int vertW) {
        int labelCol = cLeft + (vertW - "NORTH".length()) / 2;
        grid.putString(0, labelCol, "NORTH");

        for (int i = 0; i < nLanes.size(); i++) {
            LaneSnapshot lane = nLanes.get(i);
            int col = leftLaneCol(cLeft, i);
            putCentered(grid, 1, col, "v");
            putCentered(grid, 2, col, getLabel(lane));
            putCentered(grid, 3, col, "[" + lane.vehicleCount() + "]");
            putLightCentered(grid, 4, col, lane.lightColour());
        }

        putCentered(grid, 1, rightLaneCol(cLeft, leftCols, 0), "^");
    }

    private void drawSouthContent(CharGrid grid, List<LaneSnapshot> sLanes,
                                   int cLeft, int startRow, int leftCols, int vertW) {
        putCentered(grid, startRow + 3, leftLaneCol(cLeft, 0), "v");

        for (int i = 0; i < sLanes.size(); i++) {
            LaneSnapshot lane = sLanes.get(i);
            int col = rightLaneCol(cLeft, leftCols, i);
            putLightCentered(grid, startRow, col, lane.lightColour());
            putCentered(grid, startRow + 1, col, "[" + lane.vehicleCount() + "]");
            putCentered(grid, startRow + 2, col, getLabel(lane));
            putCentered(grid, startRow + 3, col, "^");
        }

        int labelCol = cLeft + (vertW - "SOUTH".length()) / 2;
        grid.putString(startRow + 4, labelCol, "SOUTH");
    }

    // --- Horizontal border ---

    private void drawHBorder(CharGrid grid, int row, int gridW, int cLeft,
                              List<Integer> seps) {
        grid.hLine(row, 0, gridW, '-');
        for (int pos : seps) {
            grid.putChar(row, cLeft + pos, '+');
        }
    }

    // --- Center section (W/E roads + intersection label) ---

    private void drawCenterSection(CharGrid grid, List<LaneSnapshot> wLanes,
                                    List<LaneSnapshot> eLanes, int startRow,
                                    int cLeft, int cRight, int horizRows, int gridW) {
        int wIn = wLanes.size();
        int eIn = eLanes.size();

        for (int r = 0; r < horizRows; r++) {
            int row = startRow + r;

            // West side
            if (r == 0) {
                putRightAligned(grid, row, cLeft, "< WEST ");
                grid.putChar(row, cLeft, '|');
            } else if (r == 1) {
                grid.hLine(row, 0, cLeft, '=');
                grid.putChar(row, cLeft, '+');
            } else if (r - 2 < wIn) {
                drawWestLane(grid, row, wLanes.get(r - 2), cLeft);
                grid.putChar(row, cLeft, '|');
            } else {
                grid.hLine(row, 0, cLeft, '-');
                grid.putChar(row, cLeft, '+');
            }

            // East side
            if (r < eIn) {
                drawEastLane(grid, row, eLanes.get(r), cRight);
                grid.putChar(row, cRight, '|');
            } else if (r == eIn) {
                grid.hLine(row, cRight + 1, gridW - cRight - 1, '=');
                grid.putChar(row, cRight, '+');
            } else if (r == eIn + 1) {
                putLeftAligned(grid, row, cRight + 1, " EAST >");
                grid.putChar(row, cRight, '|');
            } else {
                grid.hLine(row, cRight + 1, gridW - cRight - 1, '-');
                grid.putChar(row, cRight, '+');
            }
        }

        String label = "INTERSECTION";
        int contentW = cRight - cLeft - 1;
        int centerRow = startRow + horizRows / 2;
        int centerCol = cLeft + 1 + (contentW - label.length()) / 2;
        grid.putString(centerRow, centerCol, label);
    }

    private void drawWestLane(CharGrid grid, int row, LaneSnapshot lane, int cLeft) {
        String label = getLabel(lane);
        String count = "[" + lane.vehicleCount() + "]";
        String light = lightStr(lane.lightColour());
        String content = "> " + label + " " + count + " " + light + " ";

        int startCol = Math.max(0, cLeft - content.length());
        grid.putString(row, startCol, content);

        int lightStart = cLeft - light.length() - 1;
        if (lightStart >= 0) {
            grid.putColoredString(row, lightStart, light, ansiColor(lane.lightColour()));
        }
    }

    private void drawEastLane(CharGrid grid, int row, LaneSnapshot lane, int cRight) {
        String label = getLabel(lane);
        String count = "[" + lane.vehicleCount() + "]";
        String light = lightStr(lane.lightColour());
        String content = " " + light + " " + count + " " + label + " <";

        grid.putString(row, cRight + 1, content);

        int lightStart = cRight + 2;
        grid.putColoredString(row, lightStart, light, ansiColor(lane.lightColour()));
    }

    // --- Formatting helpers ---

    private void putCentered(CharGrid grid, int row, int col, String text) {
        int offset = (LW - text.length()) / 2;
        grid.putString(row, col + offset, text);
    }

    private void putLightCentered(CharGrid grid, int row, int col, TrafficLightColour colour) {
        String text = lightStr(colour);
        int offset = (LW - text.length()) / 2;
        grid.putColoredString(row, col + offset, text, ansiColor(colour));
    }

    private void putRightAligned(CharGrid grid, int row, int rightEdge, String text) {
        grid.putString(row, rightEdge - text.length(), text);
    }

    private void putLeftAligned(CharGrid grid, int row, int col, String text) {
        grid.putString(row, col, text);
    }

    private String getLabel(LaneSnapshot lane) {
        List<String> parts = new ArrayList<>();
        for (Movement m : lane.possibleMovements()) {
            if (m.to() == m.from().getLeft()) parts.add("L");
            else if (m.to() == m.from().getOpposite()) parts.add("S");
            else if (m.to() == m.from().getRight()) parts.add("R");
        }
        parts.sort(Comparator.comparingInt(s -> switch (s) {
            case "L" -> 0; case "S" -> 1; case "R" -> 2; default -> 3;
        }));
        return String.join("+", parts);
    }

    private String lightStr(TrafficLightColour colour) {
        return "[" + switch (colour) {
            case RED -> "R";
            case GREEN -> "G";
            case YELLOW -> "Y";
        } + "]";
    }

    private String ansiColor(TrafficLightColour colour) {
        return switch (colour) {
            case RED -> ANSI_RED;
            case GREEN -> ANSI_GREEN;
            case YELLOW -> ANSI_YELLOW;
        };
    }


    private List<LaneSnapshot> sortLanesByPosition(List<LaneSnapshot> lanes, Direction from) {
        List<LaneSnapshot> sorted = new ArrayList<>(lanes);
        sorted.sort(Comparator.comparingInt(lane -> laneSortKey(lane, from)));
        return sorted;
    }

    private int laneSortKey(LaneSnapshot lane, Direction from) {
        if (lane.possibleMovements().size() != 1) return 1;

        Direction to = lane.possibleMovements().get(0).to();

        if (from == Direction.NORTH || from == Direction.SOUTH) {
            if (to == Direction.WEST) return 0;
            if (to == Direction.EAST) return 2;
            return 1;
        } else {
            if (to == Direction.NORTH) return 0;
            if (to == Direction.SOUTH) return 2;
            return 1;
        }
    }
}
