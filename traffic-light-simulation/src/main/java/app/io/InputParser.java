package app.io;

import app.engine.command.AddVehicleCommand;
import app.engine.command.Command;
import app.engine.command.StepCommand;
import app.model.Direction;
import app.model.Intersection;
import app.model.Movement;
import app.model.Specification;
import app.model.TrafficLane;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InputParser {
    private static final int DEFAULT_MAX_STAY_TIME = 10;
    private static final Logger logger = Logger.getLogger(InputParser.class.getName());

    private final String filePath;
    private final SimulationReport report;

    public InputParser(String filePath, SimulationReport report) {
        this.filePath = filePath;
        this.report = report;
    }

    public Specification parse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(filePath));

        if (root == null || root.isNull()) {
            throw new InvalidInputException("Input file is empty or contains null");
        }
        if (!root.isObject()) {
            throw new InvalidInputException("Root JSON element must be an object, got: " + root.getNodeType());
        }

        ArrayList<TrafficLane> trafficLanes = parseRoads(root.get("roads"));
        int maxStayTime = parseMaxStayTime(root.get("maxStayTime"));

        boolean fastSimulation = true;
        if (root.has("fastSimulation")) {
            fastSimulation = parseBooleanField(root, "fastSimulation", "root");
        }

        Intersection intersection = new Intersection(trafficLanes, maxStayTime, fastSimulation);

        ArrayList<Command> commands = parseCommands(root.get("commands"), intersection);

        return new Specification(commands, intersection);
    }

    private ArrayList<Command> parseCommands(JsonNode commandsNode, Intersection intersection) {
        ArrayList<Command> commands = new ArrayList<>();
        if (commandsNode == null) return commands;

        if (!commandsNode.isArray()) {
            throw new InvalidInputException("\"commands\" must be an array, got: " + commandsNode.getNodeType());
        }

        for (int i = 0; i < commandsNode.size(); i++) {
            JsonNode node = commandsNode.get(i);
            String type = requireString(node, "type", "commands[" + i + "]");

            switch (type) {
                case "addVehicle" -> {
                    String vehicleId = requireString(node, "vehicleId", "commands[" + i + "]");
                    String startRoadStr = requireString(node, "startRoad", "commands[" + i + "]");
                    String endRoadStr = requireString(node, "endRoad", "commands[" + i + "]");

                    Direction startRoad = parseDirection(startRoadStr, "startRoad", i);
                    Direction endRoad = parseDirection(endRoadStr, "endRoad", i);

                    if (startRoad == endRoad) {
                        throw new InvalidInputException(
                                "commands[" + i + "]: startRoad and endRoad must differ, got: " + startRoadStr);
                    }

                    commands.add(new AddVehicleCommand(vehicleId, startRoad, endRoad, intersection));
                }
                case "step" -> commands.add(new StepCommand(intersection, report));
                default -> logger.warning("commands[" + i + "]: unknown command type \"" + type + "\", skipping");
            }
        }

        return commands;
    }

    private ArrayList<TrafficLane> parseRoads(JsonNode roadsNode) {
        Map<Direction, RoadConfig> configMap = new HashMap<>();

        for (Direction dir : Direction.values()) {
            configMap.put(dir, new RoadConfig(false, false));
        }

        if (roadsNode != null) {
            if (!roadsNode.isObject()) {
                throw new InvalidInputException("\"roads\" must be an object, got: " + roadsNode.getNodeType());
            }

            for (Direction dir : Direction.values()) {
                JsonNode dirNode = roadsNode.get(dir.name().toLowerCase());
                if (dirNode != null) {
                    if (!dirNode.isObject()) {
                        throw new InvalidInputException(
                                "roads." + dir.name().toLowerCase() + " must be an object, got: " + dirNode.getNodeType());
                    }
                    boolean left = parseBooleanField(dirNode, "leftTurnLane", "roads." + dir.name().toLowerCase());
                    boolean right = parseBooleanField(dirNode, "rightTurnLane", "roads." + dir.name().toLowerCase());
                    configMap.put(dir, new RoadConfig(left, right));
                }
            }
        }

        ArrayList<TrafficLane> lanes = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            lanes.addAll(buildLanesForDirection(dir, configMap.get(dir)));
        }
        return lanes;
    }

    private ArrayList<TrafficLane> buildLanesForDirection(Direction dir, RoadConfig config) {
        ArrayList<TrafficLane> lanes = new ArrayList<>();

        ArrayList<Movement> mainMovements = new ArrayList<>();

        if (!config.leftTurnLane() && !config.rightTurnLane()) {
            mainMovements.add(new Movement(dir, dir.getOpposite()));
            mainMovements.add(new Movement(dir, dir.getLeft()));
            mainMovements.add(new Movement(dir, dir.getRight()));
        } else {
            mainMovements.add(new Movement(dir, dir.getOpposite()));

            if (!config.leftTurnLane()) {
                mainMovements.add(new Movement(dir, dir.getLeft()));
            }
            if (!config.rightTurnLane()) {
                mainMovements.add(new Movement(dir, dir.getRight()));
            }
        }

        lanes.add(new TrafficLane(dir, mainMovements));

        if (config.leftTurnLane()) {
            ArrayList<Movement> leftMovements = new ArrayList<>();
            leftMovements.add(new Movement(dir, dir.getLeft()));
            lanes.add(new TrafficLane(dir, leftMovements));
        }

        if (config.rightTurnLane()) {
            ArrayList<Movement> rightMovements = new ArrayList<>();
            rightMovements.add(new Movement(dir, dir.getRight()));
            lanes.add(new TrafficLane(dir, rightMovements));
        }

        return lanes;
    }

    private int parseMaxStayTime(JsonNode maxStayTimeNode) {
        if (maxStayTimeNode == null) return DEFAULT_MAX_STAY_TIME;
        if (!maxStayTimeNode.isInt()) {
            throw new InvalidInputException("\"maxStayTime\" must be a positive integer, got: " + maxStayTimeNode);
        }
        int value = maxStayTimeNode.asInt();
        if (value <= 0) {
            throw new InvalidInputException("\"maxStayTime\" must be a positive integer, got: " + value);
        }
        return value;
    }

    private String requireString(JsonNode parent, String fieldName, String context) {
        JsonNode field = parent.get(fieldName);
        if (field == null || field.isNull()) {
            throw new InvalidInputException(context + ": missing required field \"" + fieldName + "\"");
        }
        return field.asText();
    }

    private Direction parseDirection(String value, String fieldName, int commandIndex) {
        try {
            return Direction.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            String allowed = Arrays.stream(Direction.values())
                    .map(d -> d.name().toLowerCase())
                    .collect(Collectors.joining(", "));
            throw new InvalidInputException(
                    "commands[" + commandIndex + "]: invalid " + fieldName + " \"" + value
                            + "\", allowed values: " + allowed, e);
        }
    }

    private boolean parseBooleanField(JsonNode parent, String fieldName, String context) {
        JsonNode field = parent.get(fieldName);
        if (field == null) return false;
        if (!field.isBoolean()) {
            throw new InvalidInputException(context + "." + fieldName + " must be a boolean, got: " + field.getNodeType());
        }
        return field.asBoolean();
    }
}
