package app.io;

import app.engine.command.AddVehicleCommand;
import app.engine.command.Command;
import app.engine.command.StepCommand;
import app.model.Direction;
import app.model.Movement;
import app.model.Specification;
import app.model.TrafficLane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputParserTest {

    private SimulationReport mockReport;
    
    @BeforeEach
    void setUp() {
        mockReport = new SimulationReport();
    }

    @Test
    void parse_ValidJson_ReturnsSpecification(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "roads": {
            "south": {
              "leftTurnLane": true,
              "rightTurnLane": true
            },
            "north": {
              "leftTurnLane": false,
              "rightTurnLane": false
            }
          },
          "maxStayTime": 15,
          "fastSimulation": true,
          "commands": [
            {
              "type": "addVehicle",
              "vehicleId": "v1",
              "startRoad": "south",
              "endRoad": "north"
            },
            {
              "type": "step"
            }
          ]
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        InputParser parser = new InputParser(inputFile.toString(), mockReport);
        Specification spec = parser.parse();

        assertNotNull(spec);
        assertEquals(15, spec.intersection().getMaxStayTime());
        
        ArrayList<Command> commands = spec.commands();
        assertEquals(2, commands.size());
        assertInstanceOf(AddVehicleCommand.class, commands.get(0));
        assertInstanceOf(StepCommand.class, commands.get(1));
    }

    @Test
    void parse_EmptyFile_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        Path inputFile = tempDir.resolve("empty.json");
        Files.writeString(inputFile, "");

        InputParser parser = new InputParser(inputFile.toString(), mockReport);
        
        assertThrows(InvalidInputException.class, parser::parse);
    }

    @Test
    void parse_InvalidRootType_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        Path inputFile = tempDir.resolve("invalid.json");
        Files.writeString(inputFile, "[]");

        InputParser parser = new InputParser(inputFile.toString(), mockReport);
        
        assertThrows(InvalidInputException.class, parser::parse);
    }

    @Test
    void parse_MissingRequiredFieldsInAddVehicleCommand_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "commands": [
            {
              "type": "addVehicle",
              "vehicleId": "v1"
            }
          ]
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        InputParser parser = new InputParser(inputFile.toString(), mockReport);
        
        Exception e = assertThrows(InvalidInputException.class, parser::parse);
        assertTrue(e.getMessage().contains("missing required field \"startRoad\""));
    }

    @Test
    void parse_SameStartAndEndRoad_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "commands": [
            {
              "type": "addVehicle",
              "vehicleId": "v1",
              "startRoad": "south",
              "endRoad": "south"
            }
          ]
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        InputParser parser = new InputParser(inputFile.toString(), mockReport);
        
        Exception e = assertThrows(InvalidInputException.class, parser::parse);
        assertTrue(e.getMessage().contains("startRoad and endRoad must differ"));
    }


    @Test
    void parse_NoTurnLanes_CreatesSingleLaneWithAllMovements(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "roads": {
            "south": { "leftTurnLane": false, "rightTurnLane": false }
          },
          "fastSimulation": true,
          "commands": []
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Specification spec = new InputParser(inputFile.toString(), mockReport).parse();

        List<TrafficLane> southLanes = spec.intersection().getTrafficLanes().stream()
                .filter(l -> l.getDirection() == Direction.SOUTH).toList();

        assertEquals(1, southLanes.size());
        List<Movement> movements = southLanes.getFirst().getPossibleMovements();
        assertEquals(3, movements.size());
        assertTrue(movements.contains(new Movement(Direction.SOUTH, Direction.NORTH)));
        assertTrue(movements.contains(new Movement(Direction.SOUTH, Direction.WEST)));
        assertTrue(movements.contains(new Movement(Direction.SOUTH, Direction.EAST)));
    }

    @Test
    void parse_LeftTurnLane_CreatesMainAndLeftLane(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "roads": {
            "south": { "leftTurnLane": true, "rightTurnLane": false }
          },
          "fastSimulation": true,
          "commands": []
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Specification spec = new InputParser(inputFile.toString(), mockReport).parse();

        List<TrafficLane> southLanes = spec.intersection().getTrafficLanes().stream()
                .filter(l -> l.getDirection() == Direction.SOUTH).toList();

        assertEquals(2, southLanes.size());

        TrafficLane mainLane = southLanes.stream()
                .filter(l -> l.getPossibleMovements().contains(new Movement(Direction.SOUTH, Direction.NORTH)))
                .findFirst().orElseThrow();
        assertEquals(2, mainLane.getPossibleMovements().size());
        assertTrue(mainLane.getPossibleMovements().contains(new Movement(Direction.SOUTH, Direction.EAST)));

        TrafficLane leftLane = southLanes.stream()
                .filter(l -> l.getPossibleMovements().contains(new Movement(Direction.SOUTH, Direction.WEST)))
                .findFirst().orElseThrow();
        assertEquals(1, leftLane.getPossibleMovements().size());
    }

    @Test
    void parse_BothTurnLanes_CreatesThreeLanes(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "roads": {
            "south": { "leftTurnLane": true, "rightTurnLane": true }
          },
          "fastSimulation": true,
          "commands": []
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Specification spec = new InputParser(inputFile.toString(), mockReport).parse();

        List<TrafficLane> southLanes = spec.intersection().getTrafficLanes().stream()
                .filter(l -> l.getDirection() == Direction.SOUTH).toList();

        assertEquals(3, southLanes.size());

        TrafficLane mainLane = southLanes.stream()
                .filter(l -> l.getPossibleMovements().contains(new Movement(Direction.SOUTH, Direction.NORTH)))
                .findFirst().orElseThrow();
        assertEquals(1, mainLane.getPossibleMovements().size());

        assertTrue(southLanes.stream().anyMatch(
                l -> l.getPossibleMovements().equals(List.of(new Movement(Direction.SOUTH, Direction.WEST)))));
        assertTrue(southLanes.stream().anyMatch(
                l -> l.getPossibleMovements().equals(List.of(new Movement(Direction.SOUTH, Direction.EAST)))));
    }


    @Test
    void parse_MaxStayTimeZero_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        String json = """
        { "maxStayTime": 0, "fastSimulation": true, "commands": [] }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Exception e = assertThrows(InvalidInputException.class,
                () -> new InputParser(inputFile.toString(), mockReport).parse());
        assertTrue(e.getMessage().contains("positive integer"));
    }

    @Test
    void parse_MaxStayTimeNegative_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        String json = """
        { "maxStayTime": -5, "fastSimulation": true, "commands": [] }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Exception e = assertThrows(InvalidInputException.class,
                () -> new InputParser(inputFile.toString(), mockReport).parse());
        assertTrue(e.getMessage().contains("positive integer"));
    }

    @Test
    void parse_MaxStayTimeDecimal_ThrowsInvalidInputException(@TempDir Path tempDir) throws IOException {
        String json = """
        { "maxStayTime": 3.5, "fastSimulation": true, "commands": [] }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Exception e = assertThrows(InvalidInputException.class,
                () -> new InputParser(inputFile.toString(), mockReport).parse());
        assertTrue(e.getMessage().contains("positive integer"));
    }

    @Test
    void parse_UnknownCommandType_IsSkipped(@TempDir Path tempDir) throws IOException {
        String json = """
        {
          "fastSimulation": true,
          "commands": [
            { "type": "unknown" },
            { "type": "step" }
          ]
        }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Specification spec = new InputParser(inputFile.toString(), mockReport).parse();

        assertEquals(1, spec.commands().size());
        assertInstanceOf(StepCommand.class, spec.commands().getFirst());
    }

    @Test
    void parse_NoRoadsSection_CreatesDefaultLanes(@TempDir Path tempDir) throws IOException {
        String json = """
        { "fastSimulation": true, "commands": [] }
        """;
        Path inputFile = tempDir.resolve("input.json");
        Files.writeString(inputFile, json);

        Specification spec = new InputParser(inputFile.toString(), mockReport).parse();

        ArrayList<TrafficLane> lanes = spec.intersection().getTrafficLanes();
        assertEquals(4, lanes.size());

        for (Direction dir : Direction.values()) {
            List<TrafficLane> dirLanes = lanes.stream()
                    .filter(l -> l.getDirection() == dir).toList();
            assertEquals(1, dirLanes.size());
            assertEquals(3, dirLanes.getFirst().getPossibleMovements().size());
        }
    }
}
