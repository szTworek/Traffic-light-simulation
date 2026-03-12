package app.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    @ParameterizedTest
    @CsvSource({"NORTH, SOUTH", "SOUTH, NORTH", "EAST, WEST", "WEST, EAST"})
    void getOpposite_ReturnsCorrectDirection(Direction input, Direction expected) {
        assertEquals(expected, input.getOpposite());
    }

    @ParameterizedTest
    @CsvSource({"NORTH, EAST", "EAST, SOUTH", "SOUTH, WEST", "WEST, NORTH"})
    void getLeft_ReturnsCorrectDirection(Direction input, Direction expected) {
        assertEquals(expected, input.getLeft());
    }

    @ParameterizedTest
    @CsvSource({"NORTH, WEST", "EAST, NORTH", "SOUTH, EAST", "WEST, SOUTH"})
    void getRight_ReturnsCorrectDirection(Direction input, Direction expected) {
        assertEquals(expected, input.getRight());
    }

    @ParameterizedTest
    @CsvSource({"NORTH, 0", "EAST, 1", "SOUTH, 2", "WEST, 3"})
    void toIndex_ReturnsCorrectIndex(Direction input, int expected) {
        assertEquals(expected, input.toIndex());
    }
}
